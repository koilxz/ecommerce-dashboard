package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 诊断 KPI DTO（diagnosis.kpi 子对象）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisKpiDTO implements Serializable {
    /** 总销售额（万 R$） */
    private Double totalSales;
    /** 销售额环比 % */
    private Double salesGrowth;
    /** 复购率 % */
    private Double repurchaseRate;
    /** 平均评分（0-5） */
    private Double avgReviewScore;
    /** 物流延误率 % */
    private Double delayRate;
    /** 退单率 % */
    private Double cancelRate;
}
