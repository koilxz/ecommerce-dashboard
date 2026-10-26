package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * KPI 汇总数据 DTO
 * 注：在 v1 基础上向后兼容地追加 复购率/平均评分/延误率/退单率 4 个字段（诊断接口使用，前端暂可不展示）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class KpiDTO implements Serializable {
    private Double totalSales;       // 总销售额 (万 R$)
    private Integer totalOrders;     // 总订单数
    private Double avgOrderValue;    // 平均客单价
    private Integer hotProducts;     // 热销商品数
    private Double salesGrowth;      // 销售额增长率 %
    private Double ordersGrowth;     // 订单量增长率 %
    private Double avgGrowth;        // 客单价增长率 %
    private Double productGrowth;    // 商品数增长率 %

    // ===== 增量字段：经营健康度（诊断/信号/预警使用）=====
    private Double repurchaseRate;   // 复购率 %
    private Double avgReviewScore;   // 平均评分（0-5）
    private Double delayRate;        // 物流延误率 %
    private Double cancelRate;       // 退单率 %
}
