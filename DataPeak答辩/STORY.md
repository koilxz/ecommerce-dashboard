# STORY.md · DataPeak 电商销售数据洞察看板 — 课程设计答辩

## ① 用户意图对齐

- **目标受众**：高校 web 课程答辩现场的评审老师与同学（技术 + 非技术混合）。
- **核心目标**：讲完后让评审相信——这是一个“真数据、真登录、真分析”的可用产品，而不仅是课程作业；并记住 Slogan「看不懂 Excel 也能看懂生意」与“代理库存口径诚实标注”这一学术诚信亮点。
- **PPT 长度**：12 页（封面 + 目录 + 4 章节扉页 + 6 内容页 + 结束页）。
- **视觉调性**：学术严谨 / 蓝白克制 / 证据驱动 / 工程化。
- **内容边界**：必讲——痛点、产品定位、技术方案、核心功能、真实数据、工程化流程、课程价值；不讲——源码逐行、部署运维细节；禁碰——夸大“实时库存”、虚构数据。

## ② 页面布局骨架

总页数 12，分 4 章，目录声明 4 章 → 必须有且仅有 4 个 `type: section` 扉页（P3/P5/P8/P11），编号 01–04 与目录逐字一致。

| 页 | 章节 | 类型 | 角色 | 节奏 | 版式 | Hero |
|----|------|------|------|------|------|------|
| P1 | — | cover | hero | peak | 全屏视觉+大标题 | ✅ |
| P2 | — | catalog | transition | transition | 左深蓝栏目录 | — |
| P3 | 01 | section | transition | transition | 章节扉页(大数字01) | — |
| P4 | 01 | content | supporting | valley | 非对称双栏(痛点+人物) | — |
| P5 | 02 | section | transition | transition | 章节扉页(大数字02) | — |
| P6 | 02 | content | hero | peak | 居中金句/巨型数字 | ✅ |
| P7 | 02 | content | supporting | valley | 左大图(架构SVG)+右技术栈 | — |
| P8 | 03 | section | transition | transition | 章节扉页(大数字03) | — |
| P9 | 03 | content | supporting | valley | 非对称双栏(五大功能) | — |
| P10| 03 | content | supporting | peak | 巨型数字+洞察(真实数据) | — |
| P11| 04 | section | transition | transition | 章节扉页(大数字04) | — |
| P12| 04 | ending | hero | peak | 工程化流程+价值+致谢 | ✅ |

Hero 占比 3/12 = 25%（落在 20–30%）；Hero 不相邻（P1/P6/P12 间隔 ≥1 个 supporting）。
非对称版式：P4/P7/P9/P10 = 4 页 ≥ 40%（P2 目录、P3/5/8/11 扉页、P6/P12 居中也算非对称基调），满足。
对称版式（N卡片横排）：0 页（全篇不用等宽横排卡片），优于“最多2页”约束。

## ③ 页面大纲

### P1 封面
- title: DataPeak · 电商销售数据洞察看板
- type: cover / role: hero / rhythm: peak / layout: 全屏视觉+大标题
- visual: L3 标题体系（深蓝顶条 + 中文主标 + 英文长 title）
- density: 字数约 40 / 图 0 / 留白约 25%
- description: 中文主标 + 英文 title「E-commerce Sales Insight Dashboard」+ 课程/汇报人/日期。

### P2 目录
- title: 目录 / CONTENTS
- type: catalog / role: transition / rhythm: transition / layout: 左深蓝栏目录
- visual: L3 编号 01–04
- description: 4 章：01 背景与痛点 / 02 产品定位与技术方案 / 03 核心功能与真实数据 / 04 工程化流程与答辩价值。

### P3 章节扉页 01
- title: 01 项目背景与痛点
- type: section / role: transition / rhythm: transition / layout: 章节扉页(大数字01)
- visual: L3 大数字 01
- description: 中小电商卖家的真实困境：数据多、不会分析、决策靠感觉。

### P4 痛点与定位
- title: 数据太多，却看不懂生意
- type: content / role: supporting / rhythm: valley / layout: 非对称双栏(左痛点文 / 右三类典型用户)
- visual: L2 三类人物标签
- anti_pattern: 禁止等宽四卡片横排；禁止把痛点写成清单小字。
- description: 左栏讲“Excel 堆满、看板不会搭、异常发现晚”三大痛点；右栏给女装店主林姐 / 代运营小吴 / 经销商陈总三类典型用户，落到“他们需要的是结论不是表格”。

### P5 章节扉页 02
- title: 02 产品定位与技术方案
- type: section / role: transition / rhythm: transition / layout: 章节扉页(大数字02)
- visual: L3 大数字 02
- description: 我们把“分析”做成人话：给结论、给建议、给预警。

### P6 产品定位 Slogan
- title: 看不懂 Excel，也能看懂生意
- type: content / role: hero / rhythm: peak / layout: 居中金句/巨型数字
- visual: 巨型中文 Slogan（≥64px）
- anti_pattern: 禁止把 Slogan 塞进小卡片；禁止配装饰小图。
- description: 一句 Slogan 立住产品价值——把专业分析翻译成卖家能行动的结论。

### P7 技术方案与系统架构
- title: 真实数据驱动的轻量全栈
- type: content / role: supporting / rhythm: valley / layout: 左大图(架构SVG)+右技术栈表
- visual: L1 架构 SVG（前端→后端→H2 真实库）
- anti_pattern: 禁止 50:50 等分双栏；禁止把架构图缩小为缩略。
- description: 左 60% 放“浏览器(ECharts)→ Spring Boot API → H2 真实库(Olist)”架构流；右 40% 列技术栈与“课程硬约束：原生 HTML/JS 不引框架”的说明。

### P8 章节扉页 03
- title: 03 核心功能与真实数据
- type: section / role: transition / rhythm: transition / layout: 章节扉页(大数字03)
- visual: L3 大数字 03
- description: 五大功能 + 真实巴西电商数据集验证。

### P9 五大核心功能
- title: 五大功能，覆盖“看数—诊断—行动”
- type: content / role: supporting / rhythm: valley / layout: 非对称双栏(左主功能 / 右辅助)
- visual: L2 图标化要点
- anti_pattern: 禁止等宽五卡片横排；禁止功能点只写名词不写价值。
- description: 真实登录 / 多店铺切换 / 智能诊断(KPI 环比) / 补货信号(代理口径) / 风险预警，每项配“卖家得到什么”。

### P10 真实数据与诚实标注
- title: 真实数据，并诚实标注口径
- type: content / role: supporting / rhythm: peak / layout: 巨型数字+洞察
- visual: 大数字（10万+ 订单 / 2016–2018 / 9 张 CSV）
- anti_pattern: 禁止把关键数字塞进图表卡角落；禁止虚构库存数据。
- description: 用 Olist 公开数据集（约 10 万订单、2016-10~2018-09、9 张 CSV）验证；补货信号明确标注“基于销售趋势推测，非实时库存”——诚实口径本身就是答辩加分项。

### P11 章节扉页 04
- title: 04 工程化流程与答辩价值
- type: section / role: transition / rhythm: transition / layout: 章节扉页(大数字04)
- visual: L3 大数字 04
- description: 团队协作流水线 + 课程收获。

### P12 工程化流程与答辩价值（结束页）
- title: 从课程作业到可发行的产品
- type: ending / role: hero / rhythm: peak / layout: 工程化流程+价值+致谢
- visual: L1 流水线 SVG（PM→架构→工程→QA）
- anti_pattern: 禁止烟花/金装饰；禁止把致谢写成大字海报。
- description: 四人角色流水线（许清楚/高见远/寇豆码/严过关）体现工程化；总结“真数据+真登录+诚实标注”的课程价值与可发行性，附致谢。
