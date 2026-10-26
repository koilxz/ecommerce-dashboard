# -*- coding: utf-8 -*-
import zipfile, os

OUT = r"D:\桌面\文件夹\2026作业\web\web项目\DataPeak答辩\AI使用记录表.xlsx"

headers = ["序号", "使用阶段 / 环节", "使用的 AI 工具 / 技能", "具体用途与关键指令",
           "AI 主要产出", "人工审核 / 修改", "耗时估计", "备注"]

rows = [
    [1, "需求与定位", "WorkBuddy 对话（需求澄清 + 多智能体团队）",
     "从角色分工截图出发，明确目标=课程高分且可上市、受众=中小电商卖家、四层改造方案、5 项产品决策（真实登录 / 多店铺切换 / 代理补货口径+诚实标注 / Slogan / 时间口径）",
     "目标澄清、受众定义、改造清单、产品决策表", "确认决策方向与优先级", "约 0.5 天", "决策由人最终拍板"],
    [2, "团队流水线分工", "WorkBuddy 多智能体（team：PM/架构/工程/QA）",
     "设定许清楚(产品)、高见远(架构)、寇豆码(工程)、严过关(QA) 四角色并分配任务卡",
     "角色职责与任务拆分", "确认分工与交接边界", "约 0.2 天", "角色名均为占位化名"],
    [3, "后端 CSV 导入排障", "WorkBuddy（代码检索 + 修改）",
     "解决 Olist CSV 的 UTF-8 BOM 导致列名找不到、review 表 review_id 主键重复、部分导入不一致导致崩溃",
     "修正后的 DatabaseInitializer（显式列名 + ROW_NUMBER 去重 + 空表先删后导）", "验证 8 张表 + 品类翻译表全部导入", "约 0.5 天", "真实数据可复现"],
    [4, "缺失接口补全", "WorkBuddy（工程子智能体）",
     "补全 /sellers、/signals、/alerts 三个控制器、服务、仓库查询与周期工具类",
     "完整的 5 接口后端", "本地 curl 联调", "约 0.5 天", "含 X-Token 鉴权"],
    [5, "前端 T7 升级", "WorkBuddy（前端生成 + 静态校验）",
     "生成欢迎页/登录、店铺切换、智能诊断、补货信号、风险预警、术语气泡等 5 个 JS 模块并改造 index.html",
     "assets 下 5 个模块 + 页面改造", "node --check 语法校验", "约 0.5 天", "原生 HTML/JS，未引框架"],
    [6, "调试与验证", "WorkBuddy（命令行端到端）",
     "用 curl 对 5 个接口做回归：公开 categories、登录拿 Token、diagnosis/sellers/signals/alerts 鉴权、无 Token 返回 401",
     "验证报告（全部 200，诊断增长 +2.8% 合理）", "浏览器实测（待本人执行）", "约 0.3 天", "localhost 走 noproxy"],
    [7, "答辩材料生成", "WorkBuddy（workbuddy-pptx 技能）",
     "生成 12 页学术风答辩 PPT：封面/目录/4 章节扉页/痛点/定位/Slogan/架构/功能/真实数据/工程化/致谢，含架构与流水线 SVG",
     "DataPeak答辩.pptx", "核对文案与数据口径", "约 0.3 天", "已通过版式校验"],
    [8, "文档与说明", "WorkBuddy（文档生成）",
     "重写 README：真实 H2 数据库、接口清单（公开 + X-Token 保护）、演示账号(demo/demo123, admin/admin123)、jar 启动命令、诚实声明",
     "README.md", "复核启动与账号", "约 0.2 天", "不含伪造库存数据"],
]

def esc(s):
    return (str(s).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
            .replace('"', "&quot;"))

cols = ["A", "B", "C", "D", "E", "F", "G", "H"]
colw = [6, 18, 26, 52, 40, 22, 10, 18]

# worksheet
sheet_rows = []
sheet_rows.append("<row r=\"1\">")
for c, h in zip(cols, headers):
    sheet_rows.append(
        f'<c r="{c}1" s="1" t="inlineStr"><is><t xml:space="preserve">{esc(h)}</t></is></c>')
sheet_rows.append("</row>")
for ri, row in enumerate(rows, start=2):
    sheet_rows.append(f'<row r="{ri}">')
    for c, val in zip(cols, row):
        sheet_rows.append(
            f'<c r="{c}{ri}" s="2" t="inlineStr"><is><t xml:space="preserve">{esc(val)}</t></is></c>')
    sheet_rows.append("</row>")

cols_xml = "".join(
    f'<col min="{i+1}" max="{i+1}" width="{w}" customWidth="1"/>'
    for i, w in enumerate(colw))

sheet_xml = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<worksheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
    f'<cols>{cols_xml}</cols>'
    f'<sheetData>{"".join(sheet_rows)}</sheetData>'
    '</worksheet>'
)

styles_xml = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<styleSheet xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main">'
    '<fonts count="2">'
    '<font><sz val="11"/><name val="Calibri"/></font>'
    '<font><b/><sz val="11"/><color rgb="FFFFFFFF"/><name val="Calibri"/></font>'
    '</fonts>'
    '<fills count="3">'
    '<fill><patternFill patternType="none"/></fill>'
    '<fill><patternFill patternType="gray125"/></fill>'
    '<fill><patternFill patternType="solid"><fgColor rgb="FF1E4FA8"/><bgColor indexed="64"/></patternFill></fill>'
    '</fills>'
    '<borders count="2">'
    '<border><left/><right/><top/><bottom/><diagonal/></border>'
    '<border>'
    '<left style="thin"><color rgb="FFD6DCE5"/></left>'
    '<right style="thin"><color rgb="FFD6DCE5"/></right>'
    '<top style="thin"><color rgb="FFD6DCE5"/></top>'
    '<bottom style="thin"><color rgb="FFD6DCE5"/></bottom>'
    '<diagonal/></border>'
    '</borders>'
    '<cellStyleXfs count="1"><xf numFmtId="0" fontId="0" fillId="0" borderId="0"/></cellStyleXfs>'
    '<cellXfs count="3">'
    '<xf numFmtId="0" fontId="0" fillId="0" borderId="0" xfId="0"/>'
    '<xf numFmtId="0" fontId="1" fillId="2" borderId="1" xfId="0" applyFont="1" applyFill="1" applyBorder="1" applyAlignment="1"><alignment horizontal="center" vertical="center" wrapText="1"/></xf>'
    '<xf numFmtId="0" fontId="0" fillId="0" borderId="1" xfId="0" applyBorder="1" applyAlignment="1"><alignment vertical="top" wrapText="1"/></xf>'
    '</cellXfs>'
    '<cellStyles count="1"><cellStyle name="Normal" xfId="0" builtinId="0"/></cellStyles>'
    '</styleSheet>'
)

content_types = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">'
    '<Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>'
    '<Default Extension="xml" ContentType="application/xml"/>'
    '<Override PartName="/xl/workbook.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml"/>'
    '<Override PartName="/xl/worksheets/sheet1.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml"/>'
    '<Override PartName="/xl/styles.xml" ContentType="application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml"/>'
    '</Types>'
)

rels = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="xl/workbook.xml"/>'
    '</Relationships>'
)

workbook = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<workbook xmlns="http://schemas.openxmlformats.org/spreadsheetml/2006/main" '
    'xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">'
    '<sheets><sheet name="AI使用记录" sheetId="1" r:id="rId1"/></sheets>'
    '</workbook>'
)

wb_rels = (
    '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>'
    '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">'
    '<Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet" Target="worksheets/sheet1.xml"/>'
    '<Relationship Id="rId2" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles" Target="styles.xml"/>'
    '</Relationships>'
)

with zipfile.ZipFile(OUT, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("[Content_Types].xml", content_types)
    z.writestr("_rels/.rels", rels)
    z.writestr("xl/workbook.xml", workbook)
    z.writestr("xl/_rels/workbook.xml.rels", wb_rels)
    z.writestr("xl/worksheets/sheet1.xml", sheet_xml)
    z.writestr("xl/styles.xml", styles_xml)

print("wrote", OUT, os.path.getsize(OUT), "bytes")
