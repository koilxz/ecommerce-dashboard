# 数据集说明（课程提交物要求：数据库脚本或说明）

## 一、数据来源

| 项目 | 内容 |
|---|---|
| 数据集 | **Brazilian E-Commerce Public Dataset by Olist**（Kaggle 公开数据集） |
| 获取地址 | https://www.kaggle.com/datasets/olistbr/brazilian-ecommerce |
| 规模 | **99,441 笔订单**（≥ 需求文档要求的 1000 条）、96,096 位唯一买家、32,951 个商品、3,095 个卖家、73 个商品品类 |
| 时间跨度 | 2016-09 ~ 2018-09（共 24 个月） |
| 语言 | 原始品类名为葡萄牙语，项目内已通过 `product_category_name_translation.csv` + `CategoryConstants` 全量映射为中文 |
| 金额单位 | 巴西雷亚尔（R$），后端统一除以 10000 转「万 R$」 |

## 二、文件清单（应放在项目根目录的 `archive/` 下）

| 文件 | 作用 | 是否必需 |
|---|---|---|
| `olist_orders_dataset.csv` | 订单主表（状态 + 5 个时间戳） | 必需 |
| `olist_order_items_dataset.csv` | 订单明细（商品、卖家、单价、运费） | 必需 |
| `olist_order_payments_dataset.csv` | 支付（方式、分期、金额） | 必需 |
| `olist_products_dataset.csv` | 商品（品类、尺寸、图片数） | 必需 |
| `olist_customers_dataset.csv` | 买家（唯一客户 ID、城市、州） | 必需 |
| `product_category_name_translation.csv` | 品类葡→英对照 | 必需 |
| `olist_order_reviews_dataset.csv` | 评价（评分、评论） | 评分相关功能需要 |
| `olist_sellers_dataset.csv` | 卖家（店铺切片） | 店铺切片需要 |
| `olist_geolocation_dataset.csv` | 邮编经纬度 | 可选（当前未使用） |

> 出于仓库体积考虑（原始 CSV 约 121MB），CSV 文件未纳入 Git。请按上表从 Kaggle 下载后放入 `archive/`。

## 三、导入机制（不是静态读取，符合技术考核点）

本项目**不使用**「直接读 CSV 返回」的做法，而是：

1. 应用启动时由 `config/DatabaseInitializer`（`ApplicationRunner`）执行自举：
   - 建表 → 播种演示账号（`demo/demo123`、`admin/admin123`）→ 若 `t_order` 为空则执行导入
2. 导入使用 H2 内置 `CSVREAD` 函数，把 CSV 直接读入表，**SQL 显式列出列名**：
   - 原因：CSV 首列含 UTF-8 BOM，用 `CSVREAD(..., NULL, ...)` 会让首列变成 `\uFEFFxxx` 而报"列不存在"
3. `t_review` 存在重复 `review_id`，导入时用 `ROW_NUMBER()` 子查询去重
4. 导入完成后创建 **9 条索引**（订单购买时间/客户、明细订单/商品/卖家、支付订单、客户唯一 ID、商品品类、评价订单），提升聚合查询性能
5. 全部业务接口通过 **JPA Repository 查询 H2 文件库**（`jdbc:h2:file:./data/dashboard`），符合"必须导入数据库后由后端查询"的要求

## 四、重建方式

删除 `data/dashboard.mv.db` 后重启应用，会自动从 `archive/` 重新导入（首次约 20–60 秒）。
