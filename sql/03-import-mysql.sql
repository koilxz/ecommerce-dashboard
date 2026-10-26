-- =============================================================
-- DataPeak · MySQL 导入脚本（兜底方案，非本项目默认路径）
-- -------------------------------------------------------------
-- ⚠️ 诚实说明：本文件**未在本机实测**（开发机未安装 MySQL 实例）。
--    项目的默认运行方式是 H2 嵌入式数据库 + 01/02 脚本，
--    本文件仅用于「换到 MySQL 演示」时参考，字段与 01-schema.sql 严格对齐。
--
-- 使用方式：
--   1. SET GLOBAL local_infile = 1;
--   2. 建库：CREATE DATABASE ecommerce_dashboard DEFAULT CHARSET utf8mb4;
--   3. 执行 01-schema.sql 建表（H2/MySQL 通用 DDL）
--   4. 在项目根目录下执行本文件：source sql/03-import-mysql.sql;
--   5. 启动时加参数 --spring.profiles.active=mysql
--
-- 与 H2 版的差异（两处必须注意）：
--   ① t_review 的 CSV 有 814 行重复 review_id，主键冲突会让整条 LOAD 语句回滚。
--      这里用 LOAD DATA ... IGNORE 跳过重复行，效果等同 H2 版的 ROW_NUMBER() 取首条。
--   ② MySQL 的 LOAD DATA 无法像 H2 的 CSVREAD 那样直接过滤表头，
--      改用 IGNORE 1 LINES 跳过首行。
-- geolocation（1,000,163 行）不导入：体积过大，且地区分布已由 t_customer 覆盖。
-- =============================================================

-- 1. 订单主表
LOAD DATA LOCAL INFILE 'archive/olist_orders_dataset.csv'
  INTO TABLE t_order
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, customer_id, order_status, order_purchase_timestamp, approved_at,
   delivered_carrier_date, delivered_customer_date, estimated_delivery_date)
  SET purchase_timestamp       = STR_TO_DATE(NULLIF(order_purchase_timestamp,''),       '%Y-%m-%d %H:%i:%s'),
      approved_at              = STR_TO_DATE(NULLIF(order_approved_at,''),              '%Y-%m-%d %H:%i:%s'),
      delivered_carrier_date   = STR_TO_DATE(NULLIF(delivered_carrier_date,''),         '%Y-%m-%d %H:%i:%s'),
      delivered_customer_date  = STR_TO_DATE(NULLIF(delivered_customer_date,''),        '%Y-%m-%d %H:%i:%s'),
      estimated_delivery_date  = STR_TO_DATE(NULLIF(estimated_delivery_date,''),        '%Y-%m-%d %H:%i:%s');

-- 2. 订单明细
LOAD DATA LOCAL INFILE 'archive/olist_order_items_dataset.csv'
  INTO TABLE t_order_item
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value)
  SET shipping_limit_date = STR_TO_DATE(NULLIF(shipping_limit_date,''), '%Y-%m-%d %H:%i:%s');

-- 3. 支付
LOAD DATA LOCAL INFILE 'archive/olist_order_payments_dataset.csv'
  INTO TABLE t_payment
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, payment_sequential, payment_type, payment_installments, payment_value);

-- 4. 评价（IGNORE 跳过 814 行重复 review_id，与 H2 版去重口径一致）
LOAD DATA LOCAL INFILE 'archive/olist_order_reviews_dataset.csv'
  IGNORE INTO TABLE t_review
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (review_id, order_id, review_score, review_comment_title, review_comment_message,
   review_creation_date, review_answer_timestamp)
  SET review_creation_date    = STR_TO_DATE(NULLIF(review_creation_date,''),    '%Y-%m-%d %H:%i:%s'),
      review_answer_timestamp = STR_TO_DATE(NULLIF(review_answer_timestamp,''), '%Y-%m-%d %H:%i:%s');

-- 5. 客户
LOAD DATA LOCAL INFILE 'archive/olist_customers_dataset.csv'
  INTO TABLE t_customer
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (customer_id, customer_unique_id, zip_code_prefix, customer_city, customer_state);

-- 6. 商品
LOAD DATA LOCAL INFILE 'archive/olist_products_dataset.csv'
  INTO TABLE t_product
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (product_id, category_name, name_length, description_length, photos_qty,
   weight_g, length_cm, height_cm, width_cm)
  SET category_name = NULLIF(category_name,'');

-- 7. 卖家 / 店铺
LOAD DATA LOCAL INFILE 'archive/olist_sellers_dataset.csv'
  INTO TABLE t_seller
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (seller_id, zip_code_prefix, seller_city, seller_state);

-- 8. 品类中英映射
LOAD DATA LOCAL INFILE 'archive/product_category_name_translation.csv'
  INTO TABLE t_category_translation
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (category_name, category_name_english);

-- 9. 演示账号无需手工插入
--    DatabaseInitializer.seedDemoUsers() 会在启动时检测 t_user 是否为空，
--    为空则用 BCryptPasswordEncoder 现场生成 demo/demo123 与 admin/admin123 的哈希写入。
--    （不在此处硬编码哈希：BCrypt 每次加盐结果不同，写死反而会让人误以为是标准答案。）
