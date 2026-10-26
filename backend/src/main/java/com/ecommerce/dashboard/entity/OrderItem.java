package com.ecommerce.dashboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体（对应 t_order_item，含 seller_id 店铺切片关键字段）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_order_item")
public class OrderItem implements Serializable {

    @EmbeddedId
    private OrderItemId id;

    @Column(name = "product_id", length = 32)
    private String productId;

    /** 卖家 ID，作为「店铺」代理（Olist 无店铺字段） */
    @Column(name = "seller_id", length = 32)
    private String sellerId;

    @Column(name = "shipping_limit_date")
    private LocalDateTime shippingLimitDate;

    @Column(name = "price", precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "freight_value", precision = 12, scale = 2)
    private BigDecimal freightValue;
}
