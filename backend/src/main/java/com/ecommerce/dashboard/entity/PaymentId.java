package com.ecommerce.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 支付记录联合主键（order_id + payment_sequential）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class PaymentId implements Serializable {

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "payment_sequential")
    private int paymentSequential;
}
