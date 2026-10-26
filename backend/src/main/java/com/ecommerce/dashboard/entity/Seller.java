package com.ecommerce.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卖家实体（对应 t_seller，作为「店铺」代理）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_seller")
public class Seller implements Serializable {

    @Id
    @Column(name = "seller_id", length = 32)
    private String sellerId;

    @Column(name = "zip_code_prefix", length = 10)
    private String zipCodePrefix;

    @Column(name = "seller_city", length = 64)
    private String sellerCity;

    @Column(name = "seller_state", length = 8)
    private String sellerState;
}
