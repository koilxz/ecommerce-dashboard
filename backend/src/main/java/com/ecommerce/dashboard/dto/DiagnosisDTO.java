package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 经营健康度诊断 DTO（/api/diagnosis 的 data）
 * 信号由服务端规则引擎生成，结论文案在服务端拼装（前端只渲染）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisDTO implements Serializable {
    private DiagnosisPeriod period;        // 所选周期
    private DiagnosisKpi kpi;              // 核心 KPI 与健康度
    private List<SignalDTO> signals;       // 信号标签数组
    private String conclusionText;         // 服务端拼装的人话结论

    /** 周期（起止 + 文案） */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisPeriod implements Serializable {
        private String start;  // yyyy-MM-dd
        private String end;    // yyyy-MM-dd
        private String label;  // 如 "所选周期"
    }

    /** 诊断用 KPI（含健康度字段） */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DiagnosisKpi implements Serializable {
        private Double totalSales;      // 总销售额（万 R$）
        private Double salesGrowth;     // 销售额环比 %
        private Double repurchaseRate;  // 复购率 %
        private Double avgReviewScore;   // 平均评分（0-5）
        private Double delayRate;        // 物流延误率 %
        private Double cancelRate;       // 退单率 %
    }
}
