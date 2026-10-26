package com.ecommerce.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付记录实体（对应 t_payment）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_payment")
public class Payment implements Serializable {

    @EmbeddedId
    private PaymentId id;

    @Column(name = "payment_type", length = 20)
    private String paymentType;

    @Column(name = "payment_installments")
    private Integer paymentInstallments;

    @Column(name = "payment_value", precision = 12, scale = 2)
    private BigDecimal paymentValue;
}
