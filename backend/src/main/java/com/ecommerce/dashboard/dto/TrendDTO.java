package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 销售趋势数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendDTO implements Serializable {
    private List<String> months;
    private List<Double> sales;      // 销售额（万）
    private List<Integer> orders;    // 订单数
}
