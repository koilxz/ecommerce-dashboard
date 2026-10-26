package com.ecommerce.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单主表实体（对应 t_order）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order")
public class Order implements Serializable {

    @Id
    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "customer_id", length = 32)
    private String customerId;

    @Column(name = "order_status", length = 20)
    private String orderStatus;

    @Column(name = "purchase_timestamp")
    private LocalDateTime purchaseTimestamp;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "delivered_carrier_date")
    private LocalDateTime deliveredCarrierDate;

    @Column(name = "delivered_customer_date")
    private LocalDateTime deliveredCustomerDate;

    @Column(name = "estimated_delivery_date")
    private LocalDateTime estimatedDeliveryDate;
}
