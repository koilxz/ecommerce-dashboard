# 电商销售数据洞察看板 DataPeak

> ** slogan：看不懂 Excel 也能看懂生意**
> 面向中小电商卖家/运营的数据洞察产品——用「人话结论 + 信号卡片」把海量订单数据变成可执行的经营动作。
> 基于 Olist 巴西电商公开数据集（约 10 万订单）的真实数据看板。

## 项目结构

```
web项目/
├── dashboard/              # 前端（原生 HTML + CSS + JS + ECharts，无框架）
│   ├── index.html          # 看板主页面（6 个功能模块 SPA + 诊断卡/预警条/店铺视角/术语气泡）
│   ├── welcome.html        # 登录门面（Slogan + 三类用户一键进入 + 登录）
│   └── assets/             # auth.js / theme.css / diagnosis.js / signals-alerts.js /
│                           #   term-glossary.js / seller-switch.js
├── backend/                # 后端（Spring Boot 3.2 + Java 17 + Spring Data JPA）
│   ├── pom.xml
│   ├── 启动后端.bat         # Windows 启动脚本
│   ├── target/dashboard-1.0.0.jar   # 已构建产物（mvn package 生成）
│   └── src/main/java/com/ecommerce/dashboard/   # controller / service / repository / config / dto
├── archive/                # Olist 原始 CSV（9 个数据集）
├── docs/                   # 产品/架构/契约文档（增量PRD、架构设计、前后端契约、角色分工）
└── ecommerce-dashboard/    # 演示文稿
```

## 后端 API 接口

基础路径：`http://localhost:8080/api`

### 公开接口（无需登录）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 品类列表 | GET | `/api/orders/categories` | A1 |
| 订单分页 | GET | `/api/orders` | A2 |
| KPI 汇总 | GET | `/api/orders/kpi` | A3 |
| 商品 TOP | GET | `/api/orders/product-ranking` | A4 |
| 销售趋势 | GET | `/api/orders/trend` | A5 |
| 品类排名 | GET | `/api/orders/category-rank` | 扩展 |
| 支付分布 | GET | `/api/orders/payment-distribution` | 扩展 |
| 用户分布 | GET | `/api/orders/user-distribution` | 扩展 |
| 卖家(店铺)列表 | GET | `/api/sellers` | 店铺视角切换（top5） |

> 上述 `/api/orders/**` 与 `/api/sellers` 均支持可选 `sellerId` 参数做店铺切片（null=全平台）。
> 金额单位统一为「万 R$」。

### 需登录接口（请求头 `X-Token`）

| 接口 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 登录 | POST | `/api/auth/login` | 校验用户名密码，返回 `token` |
| 经营诊断 | GET | `/api/diagnosis` | 规则引擎：KPI 信号 + 服务端拼装的人话结论 |
| 补货/促销信号 | GET | `/api/signals` | 按品类给出「增速 × 口碑 × 占比」行动建议（推测口径） |
| 异常预警 | GET | `/api/alerts` | 品类骤降 / 口碑下滑 / 支付突变 |

> 未携带有效 `X-Token` 访问受保护接口会返回 `401`。`X-Token` 由登录后前端存入 `sessionStorage` 并自动附带。

### 演示账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `demo` | `demo123` | 演示店主（SELLER） |
| `admin` | `admin123` | 数据管理员（ADMIN） |

Swagger 文档：`http://localhost:8080/api/swagger-ui.html`

## 启动方式

### 后端

**方式一：IDEA 启动（推荐开发）**
1. IntelliJ IDEA → Open → 选择 `backend` 文件夹
2. 等待 Maven 依赖下载
3. 运行 `DashboardApplication.java` 的 main 方法
4. 看到 `Started DashboardApplication` 即成功

**方式二：jar 启动（演示/答辩最稳）**
```bash
cd backend
mvn -B package -DskipTests        # 首次构建 jar（已构建可跳过）
java -jar target/dashboard-1.0.0.jar --server.port=8080
```
- 首次启动会自动建表并导入 `archive/*.csv`（约 10 万订单，秒级，存于 `./data/dashboard` H2 文件库）。
- 再次启动若检测到数据已存在则跳过导入，秒级启动。
- 如需强制重新导入，删除 `./data` 目录后重启即可。

### 前端

1. 先启动后端（见上）。
2. 浏览器打开 `dashboard/welcome.html` → 输入演示账号（或用三类用户一键进入）→ 进入 `index.html` 看板。
   - 也可直接用静态服务器：`python3 -m http.server` 后访问 `http://localhost:8000/dashboard/welcome.html`。
3. 看板首页第一屏即「本期经营诊断」结论卡 + 预警条 + 店铺视角下拉 + 术语气泡。

> ⚠️ 本地联调时若浏览器走了系统代理，请关闭代理或确保 `localhost` 不走代理（否则前端请求会被代理拦截返回 502）。后端已配置 CORS 全开。

## 当前阶段

- ✅ 真实数据库接入：H2 文件库 + CSVREAD 自举导入，替换原 Mock 实现（`JpaOrderServiceImpl` 为 `@Primary`，`MockOrderServiceImpl` 仅 `@Profile("mock")`）
- ✅ 新增经营诊断 / 补货促销信号 / 异常预警 / 卖家视角 / 真实登录校验
- ✅ 前端升级：欢迎页登录、诊断卡、预警条、店铺切换、术语气泡、`datapeak` 主题
- ✅ 角色分工文档 / 增量 PRD / 架构与前后端契约齐备
- ✅ 看板数字与真实数据集一致（约 10 万订单）

## 技术栈

- **前端**：原生 HTML5 / CSS3 / JavaScript + ECharts 5.5（无 Vue/React/axios，课程硬约束）
- **后端**：Spring Boot 3.2 + Spring Data JPA + springdoc-openapi
- **数据库**：H2 文件库（默认）；MySQL 8.0 作为可选 profile（`application-mysql.properties` + `import-mysql.sql`）
- **构建工具**：Maven
- **Java 版本**：17+（本地运行环境 JDK 18 亦可）
- **数据集**：Olist 巴西电商公开数据集（9 个 CSV，约 10 万订单，2016-10 ~ 2018-09）

## 诚实标注（答辩要点）

- Olist 无「店铺」字段，以 `seller_id` 作为「店铺」代理；无商品名字段，商品名 = 中文品类 + 商品ID 后 6 位。
- 「补货建议」为**销售趋势推测口径，非实时库存**（前端已显式标注「推测口径，非实时库存」）。
- 时间口径以数据集最大日期（≈2018-09）为「当前」，全站统称「所选周期」。
