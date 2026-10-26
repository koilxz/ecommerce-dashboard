package com.ecommerce.dashboard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 规则引擎阈值（集中配置，便于答辩演示「调阈值→结论变」）。
 * 对应值见 application.properties 的 datarule.* 配置。
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "datarule")
public class RuleConfig {
    private int salesGrowthStrong = 10;     // 销售额环比 >+10% → 增长强劲(绿)
    private int salesGrowthDecline = -10;   // < -10% → 下滑预警(红)
    private int repurchaseLow = 20;         // 复购率 <20% → 拉新不足(橙)
    private double reviewLow = 3.5;         // 平均评分 <3.5 → 口碑预警(红)
    private double reviewHigh = 4.2;        // ≥4.2 → 口碑优秀(绿)
    private int delayHigh = 15;             // 物流延误率 >15% → 物流预警(橙)
    private int cancelHigh = 5;             // 退单率 >5% → 退单预警(红)
    private int alertCategoryDrop = 30;     // 品类销售额环比骤降 >30% → 预警
    private double alertScoreDrop = 0.5;    // 评分环比骤降 >0.5 → 预警
    private int alertPaymentShift = 10;     // 支付方式占比突变(百分点) → 预警
    private int alertScoreDropMinReviews = 20;  // 评分环比预警的最小评论数：低于此值的品类评分波动视为小样本噪声不报
    // 品类骤降预警的“上期绝对额”下限(万 R$)：上期销售额不超过该值的品类视为小额噪声不报。
    // 取 1.0 万 R$（约 7 个月窗口月均 <1500 R$），过滤圣诞用品/内衣泳装/集市等边缘品类；
    // QA 初版建议 0.5 万，实测圣诞用品上期恰为 0.5 万会漏过边界，故按验收意图上调（改配置即可调整）。
    private double alertCategoryDropMinSales = 1.0;
}
