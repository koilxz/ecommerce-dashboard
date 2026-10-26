package com.ecommerce.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单明细联合主键（order_id + order_item_id）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class OrderItemId implements Serializable {

    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "order_item_id")
    private int orderItemId;
}
