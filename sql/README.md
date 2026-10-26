# 数据库脚本说明

> DataPeak · 电商销售数据洞察看板 · 第三组
> 本目录对应结课提交物第 7 项「数据库脚本：建表 DDL + 导入脚本 + 数据来源与字段说明」

## 一、文件清单

| 文件 | 作用 | 是否实测 |
|---|---|---|
| `01-schema.sql` | 建表 DDL：9 张表 + 13 个索引，H2 / MySQL 通用（`CREATE TABLE IF NOT EXISTS`，幂等） | ✅ 应用每次启动都会执行 |
| `02-import-h2.sql` | H2 版数据导入（`CSVREAD`），8 条 `INSERT ... SELECT` | ✅ 项目默认运行路径 |
| `03-import-mysql.sql` | MySQL 版数据导入（`LOAD DATA LOCAL INFILE`），兜底方案 | ⚠️ **未实测**（开发机无 MySQL 实例），字段与 01 严格对齐，仅供换库参考 |
| `README.md` | 本文件：数据来源、行数、字段含义、清洗规则 | — |

## 二、数据来源

| 项 | 内容 |
|---|---|
| 来源类型 | 开放数据集（公开、可溯源） |
| 数据集名称 | Brazilian E-Commerce Public Dataset by Olist |
| 来源链接 | https://www.kaggle.com/datasets/olistbr/brazilian-ecommerce |
| 获取日期 | 2026-09-28 |
| 原始文件 | `archive/` 下 9 个 CSV，UTF-8 编码（带 BOM） |
| 时间跨度 | 2016-09 ~ 2018-09（约 24 个月） |
| 货币单位 | 巴西雷亚尔（R$）；看板统一以「万 R$」展示 |

选择该数据集的原因：它是一份**真实发生过的电商交易流水**（非人工造数），同时具备订单、商品、客户、支付、评价、卖家六张可关联的事实/维度表，能同时支撑「趋势、对比、占比、排行、分布」五类图表所需的时间维度与分类维度。

## 三、真实行数（`SELECT COUNT(*)` 实测，2026-10-26）

| 表 | 行数 | 来源 CSV |
|---|---:|---|
| `t_order_item` 订单明细 | **112,650** | `olist_order_items_dataset.csv` |
| `t_payment` 支付 | 103,886 | `olist_order_payments_dataset.csv` |
| `t_order` 订单主表 | 99,441 | `olist_orders_dataset.csv` |
| `t_customer` 客户 | 99,441 | `olist_customers_dataset.csv` |
| `t_review` 评价 | 98,410 | `olist_order_reviews_dataset.csv`（原始 99,224 行，去重后 98,410） |
| `t_product` 商品 | 32,951 | `olist_products_dataset.csv` |
| `t_seller` 卖家 / 店铺 | 3,095 | `olist_sellers_dataset.csv` |
| `t_category_translation` 品类中英映射 | 71 | `product_category_name_translation.csv` |
| **业务数据合计** | **549,945** | — |
| `t_user` 系统用户 | 2 | 非 Olist 数据，启动时由 `seedDemoUsers()` 写入（demo / admin） |

未导入：`olist_geolocation_dataset.csv`（1,000,163 行）。原因见第五节第 6 条。

> **关于「数据量 ≥ 10 万行」的口径说明**：单看订单主表是 99,441 行，略低于 10 万；但看板的事实表是**订单明细 `t_order_item`，112,650 行**，全部 8 张业务表合计 **549,945 行**。所有 KPI（销售额、客单价、热销商品数）都从 `t_order_item` 聚合，因此本项目的数据量级按明细表计为 112,650 行，超过 10 万行要求。

## 四、字段说明（核心表）

### 4.1 `t_order_item` 订单明细（事实表）

| 字段名 | 类型 | 含义 | 可空 | 取值范围 / 说明 |
|---|---|---|---|---|
| `order_id` | VARCHAR(32) | 订单号 | 否 | 32 位十六进制，与 `t_order` 关联 |
| `order_item_id` | INT | 同一订单内的商品序号 | 否 | 从 1 递增；与 `order_id` 组成联合主键 |
| `product_id` | VARCHAR(32) | 商品 ID | 是 | 与 `t_product` 关联 |
| `seller_id` | VARCHAR(32) | 卖家（店铺）ID | 是 | 与 `t_seller` 关联，店铺视角切片的依据 |
| `shipping_limit_date` | TIMESTAMP | 卖家承诺发货时限 | 是 | — |
| `price` | DECIMAL(12,2) | 商品单价（R$） | 是 | > 0；**看板总销售额 = SUM(price)，不含运费** |
| `freight_value` | DECIMAL(12,2) | 运费（R$） | 是 | ≥ 0；不参与销售额口径 |

### 4.2 `t_order` 订单主表

| 字段名 | 类型 | 含义 | 可空 | 说明 |
|---|---|---|---|---|
| `order_id` | VARCHAR(32) | 订单号 | 否 | 主键 |
| `customer_id` | VARCHAR(32) | 客户 ID | 是 | 关联 `t_customer` |
| `order_status` | VARCHAR(20) | 订单状态 | 是 | delivered / shipped / canceled / invoiced / processing / unavailable / approved / created |
| `purchase_timestamp` | TIMESTAMP | 下单时间 | 是 | **趋势图与时间筛选的唯一依据** |
| `approved_at` | TIMESTAMP | 支付通过时间 | 是 | — |
| `delivered_carrier_date` | TIMESTAMP | 交付承运商时间 | 是 | — |
| `delivered_customer_date` | TIMESTAMP | 送达客户时间 | 是 | 与预估日期比较可算延误 |
| `estimated_delivery_date` | TIMESTAMP | 预估送达日期 | 是 | — |

### 4.3 其余表

- `t_customer`：`customer_id`（主键，**每单一个**）/ `customer_unique_id`（**每个自然人一个**，复购率按它去重）/ 邮编前缀 / 城市 / 州（2 位缩写）。
- `t_product`：`product_id`（主键）/ `category_name`（葡语原始品类名，可空）/ 名称长度 / 描述长度 / 图片数 / 重量 / 三边尺寸。
- `t_payment`：`order_id` + `payment_sequential` 联合主键；`payment_type`（credit_card / boleto / voucher / debit_card / not_defined）；`payment_installments` 分期数；`payment_value` 支付金额。
- `t_review`：`review_id`（主键）/ `order_id` / `review_score`（1–5）/ 评论标题 / 评论正文（CLOB）/ 创建时间 / 回复时间。
- `t_seller`：`seller_id`（主键）/ 邮编前缀 / 城市 / 州。
- `t_category_translation`：`category_name`（葡语，主键）/ `category_name_english`（英语），用于看板展示中文品类名。

## 五、清洗规则（缺失值 / 异常值 / 去重）

| # | 问题 | 规则 | 影响行数 |
|---|---|---|---:|
| 1 | **UTF-8 BOM** 导致首列名变成 `\uFEFForder_id`，H2 报 `Column order_id not found` | `CSVREAD` 第二个参数显式传列名列表（不传 NULL），首行因此被当作数据，再用 `WHERE "order_id" NOT LIKE '%order_id%'` 过滤掉表头行 | 每条语句各 1 行（共 8 行） |
| 2 | **空字符串**无法被 `PARSEDATETIME` 解析，会直接抛异常 | 所有时间与数值字段统一 `NULLIF(col,'')` 转成 NULL 后再解析/转换 | 未统计（逐字段生效） |
| 3 | **评价表重复主键**：CSV 99,224 行，但 `review_id` 只有 98,410 个唯一值 | `ROW_NUMBER() OVER (PARTITION BY review_id ORDER BY review_id)` 取 `rn = 1`，保留首条，丢弃 814 行 | **−814 行**（99,224 → 98,410） |
| 4 | **商品品类名缺失** | `NULLIF(product_category_name,'')` 存为 NULL；聚合时按「未分类」归并，不参与品类排行 | 未统计（NULL 行不进 `GROUP BY` 结果） |
| 5 | **数据残缺月份**：2016-11 整月无订单；2016-12、2018-09 等月份订单量极小（3 / 1 / 1 单） | **不删行、不插值**。看板趋势图如实呈现断点，页脚注明「2016-11 无数据」。理由：补数等于造数，会污染同比环比口径 | 0（不修改原始数据） |
| 6 | **地理表 100 万行**体积过大（CSV 约 60MB，导入后数据库膨胀） | 不导入 `olist_geolocation_dataset.csv`。地区维度改用 `t_customer.customer_state`（州级），已足够支撑分布类分析 | 跳过 1,000,163 行 |
| 7 | **金额精度** | 全部金额字段用 `DECIMAL(12,2)` 存储，避免浮点误差；展示时统一保留 1 位小数并以「万 R$」呈现 | — |

## 六、复现步骤

```bash
# 1. 确认 archive/ 下 9 个 CSV 就位（olist_*.csv + product_category_name_translation.csv）
# 2. 启动应用 —— 首次启动会自动建表 + 导入（约 20~40 秒），无需手工执行任何 SQL
java -jar backend/target/dashboard-1.0.0.jar --server.port=8080

# 3. 校验数据是否与本文档一致（H2 Console 或任一 SQL 客户端）
SELECT 't_order' AS tbl, COUNT(*) FROM t_order
UNION ALL SELECT 't_order_item', COUNT(*) FROM t_order_item
UNION ALL SELECT 't_payment', COUNT(*) FROM t_payment
UNION ALL SELECT 't_review', COUNT(*) FROM t_review
UNION ALL SELECT 't_customer', COUNT(*) FROM t_customer
UNION ALL SELECT 't_product', COUNT(*) FROM t_product
UNION ALL SELECT 't_seller', COUNT(*) FROM t_seller
UNION ALL SELECT 't_category_translation', COUNT(*) FROM t_category_translation;
```

遇到 `t_order` 有数据但 `t_review` 为空（上次导入中途退出），`DatabaseInitializer` 会判定为「半截数据」，清空 8 张业务表后整库重建，不会留下不一致状态。
