package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 支付方式分布 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDistributionDTO implements Serializable {
    private String paymentType;
    private Integer count;
    private Double percentage;
}
