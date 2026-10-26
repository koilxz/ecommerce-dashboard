-- =============================================================
-- DataPeak 数据导入脚本（H2 CSVREAD 版）
-- -------------------------------------------------------------
-- 前置条件：
--   1. 已执行 01-schema.sql 建好 9 张表；
--   2. 本文件与 archive/ 目录处于同一工作目录下（即项目根），
--      若 CSV 放在别处，请把下面每条语句中的 'archive/xxx.csv'
--      改成实际绝对路径（H2 支持 Windows 路径，如 'D:/data/xxx.csv'）。
--
-- 执行方式（二选一）：
--   A. 应用启动时自动执行：DatabaseInitializer 在 t_order 或 t_review 为空时
--      会整库重建并调用等价逻辑，无需手工跑本文件；
--   B. 手工执行：H2 Console / H2 Shell 中逐条粘贴执行。
--
-- 设计要点（也是我们踩过的坑，写在脚本里备查）：
--   ① 第二个参数传【显式列名列表】而非 NULL。Olist CSV 带 UTF-8 BOM，
--      若让 H2 自动识别表头，首列名会变成 "\uFEFForder_id"，
--      报错 "Column order_id not found"。
--   ② 传显式列名后，文件首行会被当成数据，因此用 WHERE 过滤表头行
--      （NOT LIKE '%列名%'），同时规避首行数据值被 BOM 污染。
--   ③ 空字符串必须先 NULLIF 成 NULL，否则 PARSEDATETIME('') 会抛异常。
--   ④ t_review 的 CSV 存在重复 review_id（实测 99,224 行 / 98,410 个唯一
--      review_id），直接导入会主键冲突，用 ROW_NUMBER() 分组取首条去重。
-- =============================================================

-- 1. 订单主表（99,441 行）
INSERT INTO t_order (order_id, customer_id, order_status, purchase_timestamp, approved_at,
                     delivered_carrier_date, delivered_customer_date, estimated_delivery_date)
SELECT "order_id", "customer_id", "order_status",
       PARSEDATETIME(NULLIF("order_purchase_timestamp",''),'yyyy-MM-dd HH:mm:ss'),
       PARSEDATETIME(NULLIF("order_approved_at",''),'yyyy-MM-dd HH:mm:ss'),
       PARSEDATETIME(NULLIF("order_delivered_carrier_date",''),'yyyy-MM-dd HH:mm:ss'),
       PARSEDATETIME(NULLIF("order_delivered_customer_date",''),'yyyy-MM-dd HH:mm:ss'),
       PARSEDATETIME(NULLIF("order_estimated_delivery_date",''),'yyyy-MM-dd HH:mm:ss')
FROM CSVREAD('archive/olist_orders_dataset.csv',
       'order_id,customer_id,order_status,order_purchase_timestamp,order_approved_at,order_delivered_carrier_date,order_delivered_customer_date,order_estimated_delivery_date',
       'charset=UTF-8')
WHERE "order_id" NOT LIKE '%order_id%';

-- 2. 订单明细（112,650 行）—— 看板的"事实表"，销售额、订单数、热销商品数都从这张表聚合
INSERT INTO t_order_item (order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value)
SELECT "order_id", CAST(NULLIF("order_item_id",'') AS INT), "product_id", "seller_id",
       PARSEDATETIME(NULLIF("shipping_limit_date",''),'yyyy-MM-dd HH:mm:ss'),
       CAST(NULLIF("price",'') AS DECIMAL(12,2)),
       CAST(NULLIF("freight_value",'') AS DECIMAL(12,2))
FROM CSVREAD('archive/olist_order_items_dataset.csv',
       'order_id,order_item_id,product_id,seller_id,shipping_limit_date,price,freight_value',
       'charset=UTF-8')
WHERE "order_id" NOT LIKE '%order_id%';

-- 3. 支付（103,886 行）
INSERT INTO t_payment (order_id, payment_sequential, payment_type, payment_installments, payment_value)
SELECT "order_id", CAST(NULLIF("payment_sequential",'') AS INT), "payment_type",
       CAST(NULLIF("payment_installments",'') AS INT),
       CAST(NULLIF("payment_value",'') AS DECIMAL(12,2))
FROM CSVREAD('archive/olist_order_payments_dataset.csv',
       'order_id,payment_sequential,payment_type,payment_installments,payment_value',
       'charset=UTF-8')
WHERE "order_id" NOT LIKE '%order_id%';

-- 4. 评价（CSV 99,224 行 → 去重后 98,410 行）
--    注意：子查询内外列名大小写必须一致——CSVREAD 按显式列名保留大小写，
--    外层同样要用双引号引用，否则 H2 会把未加引号的列名转成大写而找不到。
INSERT INTO t_review (review_id, order_id, review_score, review_comment_title,
                      review_comment_message, review_creation_date, review_answer_timestamp)
SELECT "review_id", "order_id", "review_score", "review_comment_title",
       "review_comment_message", "review_creation_date", "review_answer_timestamp"
FROM (
    SELECT "review_id", "order_id",
           CAST(NULLIF("review_score",'') AS INT) AS "review_score",
           "review_comment_title", "review_comment_message",
           PARSEDATETIME(NULLIF("review_creation_date",''),'yyyy-MM-dd HH:mm:ss')  AS "review_creation_date",
           PARSEDATETIME(NULLIF("review_answer_timestamp",''),'yyyy-MM-dd HH:mm:ss') AS "review_answer_timestamp",
           ROW_NUMBER() OVER (PARTITION BY "review_id" ORDER BY "review_id") AS rn
    FROM CSVREAD('archive/olist_order_reviews_dataset.csv',
           'review_id,order_id,review_score,review_comment_title,review_comment_message,review_creation_date,review_answer_timestamp',
           'charset=UTF-8')
    WHERE "review_id" NOT LIKE '%review_id%'
) WHERE rn = 1;

-- 5. 客户（99,441 行）
INSERT INTO t_customer (customer_id, customer_unique_id, zip_code_prefix, customer_city, customer_state)
SELECT "customer_id", "customer_unique_id", "customer_zip_code_prefix",
       "customer_city", "customer_state"
FROM CSVREAD('archive/olist_customers_dataset.csv',
       'customer_id,customer_unique_id,customer_zip_code_prefix,customer_city,customer_state',
       'charset=UTF-8')
WHERE "customer_id" NOT LIKE '%customer_id%';

-- 6. 商品（32,951 行）
INSERT INTO t_product (product_id, category_name, name_length, description_length, photos_qty,
                       weight_g, length_cm, height_cm, width_cm)
SELECT "product_id", NULLIF("product_category_name",''),
       CAST(NULLIF("product_name_lenght",'')        AS INT),
       CAST(NULLIF("product_description_lenght",'') AS INT),
       CAST(NULLIF("product_photos_qty",'')         AS INT),
       CAST(NULLIF("product_weight_g",'')           AS INT),
       CAST(NULLIF("product_length_cm",'')          AS INT),
       CAST(NULLIF("product_height_cm",'')          AS INT),
       CAST(NULLIF("product_width_cm",'')           AS INT)
FROM CSVREAD('archive/olist_products_dataset.csv',
       'product_id,product_category_name,product_name_lenght,product_description_lenght,product_photos_qty,product_weight_g,product_length_cm,product_height_cm,product_width_cm',
       'charset=UTF-8')
WHERE "product_id" NOT LIKE '%product_id%';

-- 7. 卖家 / 店铺（3,095 行）—— 看板"店铺视角"切片的数据来源
INSERT INTO t_seller (seller_id, zip_code_prefix, seller_city, seller_state)
SELECT "seller_id", "seller_zip_code_prefix", "seller_city", "seller_state"
FROM CSVREAD('archive/olist_sellers_dataset.csv',
       'seller_id,seller_zip_code_prefix,seller_city,seller_state',
       'charset=UTF-8')
WHERE "seller_id" NOT LIKE '%seller_id%';

-- 8. 品类中英映射（70 行）—— CSV 只有两列且无表头，用 C1/C2 占位后过滤
INSERT INTO t_category_translation (category_name, category_name_english)
SELECT C1, C2
FROM CSVREAD('archive/product_category_name_translation.csv', 'C1, C2', 'charset=UTF-8')
WHERE C1 IS NOT NULL AND C1 NOT LIKE '%product_category_name%';

-- =============================================================
-- 导入后校验（把结果与文档《二、数据设计》中的行数逐一比对）
-- =============================================================
-- SELECT 't_order'                AS tbl, COUNT(*) FROM t_order
-- UNION ALL SELECT 't_order_item',          COUNT(*) FROM t_order_item
-- UNION ALL SELECT 't_payment',             COUNT(*) FROM t_payment
-- UNION ALL SELECT 't_review',              COUNT(*) FROM t_review
-- UNION ALL SELECT 't_customer',            COUNT(*) FROM t_customer
-- UNION ALL SELECT 't_product',             COUNT(*) FROM t_product
-- UNION ALL SELECT 't_seller',              COUNT(*) FROM t_seller
-- UNION ALL SELECT 't_category_translation',COUNT(*) FROM t_category_translation;
