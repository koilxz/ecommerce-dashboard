package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户分布 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDistributionDTO implements Serializable {
    private Integer newUserCount;
    private Integer repeatUserCount;
    private Double newUserPercentage;
    private Double repeatPercentage;
    private Integer highValueCount;
    private Double highValuePercentage;
}
