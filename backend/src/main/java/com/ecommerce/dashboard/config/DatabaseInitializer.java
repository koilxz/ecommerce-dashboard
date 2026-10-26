package com.ecommerce.dashboard.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据库自举初始化（T0）：
 *  1. 执行 classpath 下的 schema.sql 建表（H2 / 通用 DDL）；
 *  2. 当 t_order 为空时，用 H2 CSVREAD 一次性导入 archive/*.csv（约 45 万行，秒级）。
 * 仅默认 H2 数据源下执行导入；MySQL profile 由 import-mysql.sql 兜底（代码零改动）。
 * geolocation（100 万行）不导入（地区分布已由 t_customer 覆盖）。
 */
@Component
public class DatabaseInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final JdbcTemplate jdbcTemplate;

    private final DataReadyState dataReadyState;

    @Value("${import.csv-path:archive/}")
    private String csvPath;

    @Value("${spring.datasource.url:}")
    private String dataSourceUrl;

@Autowired
public DatabaseInitializer(JdbcTemplate jdbcTemplate, DataReadyState dataReadyState) {
    this.jdbcTemplate = jdbcTemplate;
    this.dataReadyState = dataReadyState;
}

    @Override
    public void run(ApplicationArguments args) {
        // 仅 H2 数据源下做 CSVREAD 自举导入；MySQL profile 交给 import-mysql.sql
        if (dataSourceUrl == null || !dataSourceUrl.toLowerCase().contains("h2")) {
            // MySQL profile：数据由 import-mysql.sql 预先导入；索引尽力创建后直接标记就绪
            createIndexes();
            dataReadyState.markReady("数据就绪（MySQL profile，数据由 import-mysql.sql 预先导入）");
            return;
        }

        // 就绪检查：进入导入流程前先标记“导入中”（/api/ready 返回 503）
        dataReadyState.markImporting();

        // 1. 建表
        executeSchema();

        // 1.5 建表后播种演示账号（必须在建表之后，否则 PostConstruct 阶段查 t_user 会失败）
        seedDemoUsers();

        // 2. 任意核心表为空（含上次导入中途崩溃留下的半截数据）则整库重建后导入，
        //    避免「t_order 有数但 t_review 空」导致后续启动跳过导入而数据不一致。
        Integer orderCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_order", Integer.class);
        Integer reviewCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_review", Integer.class);
        if ((orderCount == null || orderCount == 0) || (reviewCount == null || reviewCount == 0)) {
            // 清空可能残留的半截数据（表已由 executeSchema 建好，空表 DELETE 无害）
            for (String t : new String[]{"t_review","t_seller","t_product","t_customer","t_payment","t_order_item","t_order","t_category_translation"}) {
                jdbcTemplate.execute("DELETE FROM " + t);
            }
            importCsv();
        }

        // 3. 索引建在导入之后：每次启动都执行（CREATE INDEX IF NOT EXISTS 幂等），
        //    加速 diagnosis/signals/alerts 冷查询（此前无索引需数十秒）。
        createIndexes();

        // 4. 数据导入 / 校验完成，标记就绪（/api/ready 返回 200）
        dataReadyState.markReady("数据已就绪");
    }

    /** 读取并执行 classpath schema.sql（按 ; 拆分多语句） */
    private void executeSchema() {
        try {
            ClassPathResource resource = new ClassPathResource("schema.sql");
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }
            for (String stmt : sb.toString().split(";")) {
                String s = stmt.trim();
                if (!s.isEmpty()) {
                    jdbcTemplate.execute(s);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("执行 schema.sql 失败：" + e.getMessage(), e);
        }
    }

    /** 建表后写入 2 个 BCrypt 演示账号（demo/demo123、admin/admin123） */
    private void seedDemoUsers() {
        Integer n = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM t_user", Integer.class);
        if (n != null && n > 0) {
            return;
        }
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder =
                new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        jdbcTemplate.update("INSERT INTO t_user (username, password, display_name, role) VALUES (?,?,?,?)",
                "demo", encoder.encode("demo123"), "演示店主", "SELLER");
        jdbcTemplate.update("INSERT INTO t_user (username, password, display_name, role) VALUES (?,?,?,?)",
                "admin", encoder.encode("admin123"), "数据管理员", "ADMIN");
    }

    /** 解析真实的 CSV 目录（兼容从 backend/ 启动时的 ../archive/ 回退） */
    private String resolveCsvDir() {
        String[] candidates = {csvPath, "../archive/", "archive/", "./archive/"};
        for (String c : candidates) {
            String dir = c.endsWith("/") ? c : c + "/";
            if (Files.exists(Paths.get(dir + "olist_orders_dataset.csv"))) {
                return dir;
            }
        }
        // 最后兜底：直接用配置值
        return csvPath.endsWith("/") ? csvPath : csvPath + "/";
    }

    /**
     * CSVREAD 自举导入（各表 INSERT ... SELECT，时间戳 PARSEDATETIME 解析）。
     * 关键点：第二个参数传【显式列名列表】，而不传 NULL。
     *   - 传 NULL 时 H2 会把文件首行当列名；但 Olist CSV 带 UTF-8 BOM，首列会变成
     *     \uFEFForder_id，导致 "Column order_id not found"。
     *   - 传显式列名后，首行被视为【数据】，再用 WHERE 过滤掉表头行（NOT LIKE '%主键列名%'
     *     同时规避首行数据值被 BOM 污染的情况）。与 t_category_translation 的写法保持一致。
     */
    private void importCsv() {
        String dir = resolveCsvDir();
        List<String> sqls = new ArrayList<>();

        // 订单主表
        sqls.add("INSERT INTO t_order (order_id, customer_id, order_status, purchase_timestamp, approved_at, " +
                "delivered_carrier_date, delivered_customer_date, estimated_delivery_date) " +
                "SELECT \"order_id\", \"customer_id\", \"order_status\", " +
                "PARSEDATETIME(NULLIF(\"order_purchase_timestamp\",''),'yyyy-MM-dd HH:mm:ss'), " +
                "PARSEDATETIME(NULLIF(\"order_approved_at\",''),'yyyy-MM-dd HH:mm:ss'), " +
                "PARSEDATETIME(NULLIF(\"order_delivered_carrier_date\",''),'yyyy-MM-dd HH:mm:ss'), " +
                "PARSEDATETIME(NULLIF(\"order_delivered_customer_date\",''),'yyyy-MM-dd HH:mm:ss'), " +
                "PARSEDATETIME(NULLIF(\"order_estimated_delivery_date\",''),'yyyy-MM-dd HH:mm:ss') " +
                "FROM CSVREAD('" + dir + "olist_orders_dataset.csv', " +
                "'order_id,customer_id,order_status,order_purchase_timestamp,order_approved_at,order_delivered_carrier_date,order_delivered_customer_date,order_estimated_delivery_date', 'charset=UTF-8') " +
                "WHERE \"order_id\" NOT LIKE '%order_id%'");

        // 订单明细
        sqls.add("INSERT INTO t_order_item (order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value) " +
                "SELECT \"order_id\", CAST(NULLIF(\"order_item_id\",'') AS INT), \"product_id\", \"seller_id\", " +
                "PARSEDATETIME(NULLIF(\"shipping_limit_date\",''),'yyyy-MM-dd HH:mm:ss'), " +
                "CAST(NULLIF(\"price\",'') AS DECIMAL(12,2)), CAST(NULLIF(\"freight_value\",'') AS DECIMAL(12,2)) " +
                "FROM CSVREAD('" + dir + "olist_order_items_dataset.csv', " +
                "'order_id,order_item_id,product_id,seller_id,shipping_limit_date,price,freight_value', 'charset=UTF-8') " +
                "WHERE \"order_id\" NOT LIKE '%order_id%'");

        // 支付
        sqls.add("INSERT INTO t_payment (order_id, payment_sequential, payment_type, payment_installments, payment_value) " +
                "SELECT \"order_id\", CAST(NULLIF(\"payment_sequential\",'') AS INT), \"payment_type\", " +
                "CAST(NULLIF(\"payment_installments\",'') AS INT), CAST(NULLIF(\"payment_value\",'') AS DECIMAL(12,2)) " +
                "FROM CSVREAD('" + dir + "olist_order_payments_dataset.csv', " +
                "'order_id,payment_sequential,payment_type,payment_installments,payment_value', 'charset=UTF-8') " +
                "WHERE \"order_id\" NOT LIKE '%order_id%'");

        // 评价（Olist 评价 CSV 存在重复 review_id 行，主键冲突；用 ROW_NUMBER 按 review_id 去重，保留首条）
        // 注意：子查询内外列名大小写必须一致——CSVREAD 列名按显式列名大小写保留，外层需同样用双引号引用。
        sqls.add("INSERT INTO t_review (review_id, order_id, review_score, review_comment_title, review_comment_message, " +
                "review_creation_date, review_answer_timestamp) " +
                "SELECT \"review_id\", \"order_id\", \"review_score\", \"review_comment_title\", \"review_comment_message\", " +
                "\"review_creation_date\", \"review_answer_timestamp\" FROM (" +
                "SELECT \"review_id\", \"order_id\", CAST(NULLIF(\"review_score\",'') AS INT) AS \"review_score\", " +
                "\"review_comment_title\", \"review_comment_message\", " +
                "PARSEDATETIME(NULLIF(\"review_creation_date\",''),'yyyy-MM-dd HH:mm:ss') AS \"review_creation_date\", " +
                "PARSEDATETIME(NULLIF(\"review_answer_timestamp\",''),'yyyy-MM-dd HH:mm:ss') AS \"review_answer_timestamp\", " +
                "ROW_NUMBER() OVER (PARTITION BY \"review_id\" ORDER BY \"review_id\") AS rn " +
                "FROM CSVREAD('" + dir + "olist_order_reviews_dataset.csv', " +
                "'review_id,order_id,review_score,review_comment_title,review_comment_message,review_creation_date,review_answer_timestamp', 'charset=UTF-8') " +
                "WHERE \"review_id\" NOT LIKE '%review_id%') WHERE rn = 1");

        // 客户
        sqls.add("INSERT INTO t_customer (customer_id, customer_unique_id, zip_code_prefix, customer_city, customer_state) " +
                "SELECT \"customer_id\", \"customer_unique_id\", \"customer_zip_code_prefix\", \"customer_city\", \"customer_state\" " +
                "FROM CSVREAD('" + dir + "olist_customers_dataset.csv', " +
                "'customer_id,customer_unique_id,customer_zip_code_prefix,customer_city,customer_state', 'charset=UTF-8') " +
                "WHERE \"customer_id\" NOT LIKE '%customer_id%'");

        // 商品
        sqls.add("INSERT INTO t_product (product_id, category_name, name_length, description_length, photos_qty, " +
                "weight_g, length_cm, height_cm, width_cm) " +
                "SELECT \"product_id\", NULLIF(\"product_category_name\",''), " +
                "CAST(NULLIF(\"product_name_lenght\",'') AS INT), CAST(NULLIF(\"product_description_lenght\",'') AS INT), " +
                "CAST(NULLIF(\"product_photos_qty\",'') AS INT), CAST(NULLIF(\"product_weight_g\",'') AS INT), " +
                "CAST(NULLIF(\"product_length_cm\",'') AS INT), CAST(NULLIF(\"product_height_cm\",'') AS INT), " +
                "CAST(NULLIF(\"product_width_cm\",'') AS INT) " +
                "FROM CSVREAD('" + dir + "olist_products_dataset.csv', " +
                "'product_id,product_category_name,product_name_lenght,product_description_lenght,product_photos_qty,product_weight_g,product_length_cm,product_height_cm,product_width_cm', 'charset=UTF-8') " +
                "WHERE \"product_id\" NOT LIKE '%product_id%'");

        // 卖家
        sqls.add("INSERT INTO t_seller (seller_id, zip_code_prefix, seller_city, seller_state) " +
                "SELECT \"seller_id\", \"seller_zip_code_prefix\", \"seller_city\", \"seller_state\" " +
                "FROM CSVREAD('" + dir + "olist_sellers_dataset.csv', " +
                "'seller_id,seller_zip_code_prefix,seller_city,seller_state', 'charset=UTF-8') " +
                "WHERE \"seller_id\" NOT LIKE '%seller_id%'");

        // 品类中英映射（首行是表头，显式列名 + 过滤表头行）
        sqls.add("INSERT INTO t_category_translation (category_name, category_name_english) " +
                "SELECT C1, C2 FROM CSVREAD('" + dir + "product_category_name_translation.csv', 'C1, C2', 'charset=UTF-8') " +
                "WHERE C1 IS NOT NULL AND C1 NOT LIKE '%product_category_name%'");

        for (String sql : sqls) {
            jdbcTemplate.execute(sql);
        }
    }

    /**
     * 创建查询索引（借鉴队友版 DbIndexInitializer，解决 diagnosis/signals/alerts 冷查慢）。
     * 每次启动都执行，CREATE INDEX IF NOT EXISTS 幂等；逐条 try/catch + log.warn，
     * 单条失败（如 MySQL 不支持 IF NOT EXISTS）不影响启动，仅记录告警。
     */
    private void createIndexes() {
        String[] ddl = {
                "CREATE INDEX IF NOT EXISTS idx_t_order_purchase ON t_order(purchase_timestamp)",
                "CREATE INDEX IF NOT EXISTS idx_t_order_customer ON t_order(customer_id)",
                "CREATE INDEX IF NOT EXISTS idx_t_item_order ON t_order_item(order_id)",
                "CREATE INDEX IF NOT EXISTS idx_t_item_product ON t_order_item(product_id)",
                // 我们比队友版多这条：服务 seller 视角切片查询（t_order_item.seller_id）
                "CREATE INDEX IF NOT EXISTS idx_t_item_seller ON t_order_item(seller_id)",
                "CREATE INDEX IF NOT EXISTS idx_t_payment_order ON t_payment(order_id)",
                "CREATE INDEX IF NOT EXISTS idx_t_customer_unique ON t_customer(customer_unique_id)",
                "CREATE INDEX IF NOT EXISTS idx_t_product_category ON t_product(category_name)",
                // 服务品类平均评分 JOIN（t_review.order_id -> t_order_item）
                "CREATE INDEX IF NOT EXISTS idx_t_review_order ON t_review(order_id)"
        };
        for (String sql : ddl) {
            try {
                jdbcTemplate.execute(sql);
            } catch (Exception e) {
                log.warn("创建索引失败（可忽略）：{} -> {}", sql, e.getMessage());
            }
        }
    }
}
