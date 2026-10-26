package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO implements Serializable {
    private String orderId;
    private String productName;
    private String category;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalAmount;
    private LocalDateTime purchaseTime;
    private String paymentType;
    private String orderStatus;
}
