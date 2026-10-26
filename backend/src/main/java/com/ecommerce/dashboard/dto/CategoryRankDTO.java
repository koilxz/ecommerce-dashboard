package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 品类销售排名 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRankDTO implements Serializable {
    private String categoryName;     // 品类名（中文）
    private String categoryNameEn;   // 品类名（英文）
    private Double salesAmount;      // 销售额（万）
    private Integer orderCount;      // 订单数
}
