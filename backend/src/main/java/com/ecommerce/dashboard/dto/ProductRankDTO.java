package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 商品销量排名 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRankDTO implements Serializable {
    private String productId;
    private String productName;
    private String category;
    private Double price;
    private Integer salesCount;   // 销售件数
    private Double salesAmount;   // 销售额
}
