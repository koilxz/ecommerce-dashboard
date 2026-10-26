-- =============================================================
-- DataPeak 建表脚本（H2 / MySQL 通用，幂等）
-- 与 backend/src/main/resources/schema.sql 内容一致，便于评审与 MySQL 兜底使用。
-- 表名统一加 t_ 前缀，规避保留字。
-- =============================================================

CREATE TABLE IF NOT EXISTS t_order (
    order_id                  VARCHAR(32)  PRIMARY KEY,
    customer_id               VARCHAR(32),
    order_status              VARCHAR(20),
    purchase_timestamp        TIMESTAMP,
    approved_at               TIMESTAMP,
    delivered_carrier_date    TIMESTAMP,
    delivered_customer_date   TIMESTAMP,
    estimated_delivery_date   TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_order_item (
    order_id            VARCHAR(32)  NOT NULL,
    order_item_id       INT          NOT NULL,
    product_id          VARCHAR(32),
    seller_id           VARCHAR(32),
    shipping_limit_date TIMESTAMP,
    price               DECIMAL(12,2),
    freight_value       DECIMAL(12,2),
    PRIMARY KEY (order_id, order_item_id)
);

CREATE TABLE IF NOT EXISTS t_payment (
    order_id             VARCHAR(32) NOT NULL,
    payment_sequential   INT         NOT NULL,
    payment_type         VARCHAR(20),
    payment_installments INT,
    payment_value        DECIMAL(12,2),
    PRIMARY KEY (order_id, payment_sequential)
);

CREATE TABLE IF NOT EXISTS t_review (
    review_id                VARCHAR(32) PRIMARY KEY,
    order_id                 VARCHAR(32),
    review_score             INT,
    review_comment_title     VARCHAR(255),
    review_comment_message   CLOB,
    review_creation_date     TIMESTAMP,
    review_answer_timestamp  TIMESTAMP
);

CREATE TABLE IF NOT EXISTS t_customer (
    customer_id         VARCHAR(32) PRIMARY KEY,
    customer_unique_id  VARCHAR(32),
    zip_code_prefix     VARCHAR(10),
    customer_city       VARCHAR(64),
    customer_state      VARCHAR(8)
);

CREATE TABLE IF NOT EXISTS t_product (
    product_id           VARCHAR(32) PRIMARY KEY,
    category_name        VARCHAR(64),
    name_length          INT,
    description_length   INT,
    photos_qty          INT,
    weight_g             INT,
    length_cm            INT,
    height_cm            INT,
    width_cm             INT
);

CREATE TABLE IF NOT EXISTS t_seller (
    seller_id        VARCHAR(32) PRIMARY KEY,
    zip_code_prefix  VARCHAR(10),
    seller_city      VARCHAR(64),
    seller_state     VARCHAR(8)
);

CREATE TABLE IF NOT EXISTS t_category_translation (
    category_name           VARCHAR(64) PRIMARY KEY,
    category_name_english   VARCHAR(64)
);

CREATE TABLE IF NOT EXISTS t_user (
    username      VARCHAR(32) PRIMARY KEY,
    password      VARCHAR(64),
    display_name  VARCHAR(64),
    role          VARCHAR(20)
);

CREATE INDEX IF NOT EXISTS idx_order_item_seller       ON t_order_item (seller_id);
CREATE INDEX IF NOT EXISTS idx_order_purchase_status  ON t_order (purchase_timestamp, order_status);
CREATE INDEX IF NOT EXISTS idx_review_order_score     ON t_review (order_id, review_score);
CREATE INDEX IF NOT EXISTS idx_order_item_product     ON t_order_item (product_id);
