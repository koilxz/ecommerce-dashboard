-- =============================================================
-- DataPeak · MySQL 导入脚本（兜底方案）
-- 使用方式：切到 mysql profile 后执行本文件（需 local_infile=1）
--   SET GLOBAL local_infile = 1;
--   在 mysql 客户端执行： source database/import-mysql.sql;
-- 路径请按实际 archive 位置修改（下方用相对路径，需从项目根目录执行）
-- 注意：geolocation 表不导入（体积过大且地区分布已由 t_customer 覆盖）
-- =============================================================

LOAD DATA LOCAL INFILE 'archive/olist_orders_dataset.csv'
  INTO TABLE t_order
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, customer_id, order_status, order_purchase_timestamp, order_approved_at,
   order_delivered_carrier_date, order_delivered_customer_date, order_estimated_delivery_date)
  SET purchase_timestamp      = NULLIF(@dummy:=NULL, NULL),  -- 占位，下方统一处理
      purchase_timestamp      = STR_TO_DATE(NULLIF(order_purchase_timestamp,''), '%Y-%m-%d %H:%i:%s'),
      approved_at            = STR_TO_DATE(NULLIF(order_approved_at,''), '%Y-%m-%d %H:%i:%s'),
      delivered_carrier_date = STR_TO_DATE(NULLIF(order_delivered_carrier_date,''), '%Y-%m-%d %H:%i:%s'),
      delivered_customer_date= STR_TO_DATE(NULLIF(order_delivered_customer_date,''), '%Y-%m-%d %H:%i:%s'),
      estimated_delivery_date= STR_TO_DATE(NULLIF(order_estimated_delivery_date,''), '%Y-%m-%d %H:%i:%s');

LOAD DATA LOCAL INFILE 'archive/olist_order_items_dataset.csv'
  INTO TABLE t_order_item
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, order_item_id, product_id, seller_id, shipping_limit_date, price, freight_value)
  SET shipping_limit_date = STR_TO_DATE(NULLIF(shipping_limit_date,''), '%Y-%m-%d %H:%i:%s');

LOAD DATA LOCAL INFILE 'archive/olist_order_payments_dataset.csv'
  INTO TABLE t_payment
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (order_id, payment_sequential, payment_type, payment_installments, payment_value);

LOAD DATA LOCAL INFILE 'archive/olist_order_reviews_dataset.csv'
  INTO TABLE t_review
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (review_id, order_id, review_score, review_comment_title, review_comment_message,
   review_creation_date, review_answer_timestamp)
  SET review_creation_date    = STR_TO_DATE(NULLIF(review_creation_date,''), '%Y-%m-%d %H:%i:%s'),
      review_answer_timestamp = STR_TO_DATE(NULLIF(review_answer_timestamp,''), '%Y-%m-%d %H:%i:%s');

LOAD DATA LOCAL INFILE 'archive/olist_customers_dataset.csv'
  INTO TABLE t_customer
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (customer_id, customer_unique_id, zip_code_prefix, customer_city, customer_state);

LOAD DATA LOCAL INFILE 'archive/olist_products_dataset.csv'
  INTO TABLE t_product
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (product_id, category_name, name_length, description_length, photos_qty,
   weight_g, length_cm, height_cm, width_cm)
  SET category_name = NULLIF(category_name,'');

LOAD DATA LOCAL INFILE 'archive/olist_sellers_dataset.csv'
  INTO TABLE t_seller
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (seller_id, zip_code_prefix, seller_city, seller_state);

LOAD DATA LOCAL INFILE 'archive/product_category_name_translation.csv'
  INTO TABLE t_category_translation
  CHARACTER SET utf8mb4
  FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
  LINES TERMINATED BY '\n'
  IGNORE 1 LINES
  (category_name, category_name_english);
