package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品概览 DTO（商品页 KPI 用，全部来自商品表真实统计）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductOverviewDTO implements Serializable {
    private Long totalProducts;      // 商品总数（商品表行数）
    private Integer totalCategories; // 品类数（商品表去重品类）
}
