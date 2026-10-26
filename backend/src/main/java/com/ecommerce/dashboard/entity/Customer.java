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
 * 客户实体（对应 t_customer，customer_unique_id 用于复购率计算）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_customer")
public class Customer implements Serializable {

    @Id
    @Column(name = "customer_id", length = 32)
    private String customerId;

    @Column(name = "customer_unique_id", length = 32)
    private String customerUniqueId;

    @Column(name = "zip_code_prefix", length = 10)
    private String zipCodePrefix;

    @Column(name = "customer_city", length = 64)
    private String customerCity;

    @Column(name = "customer_state", length = 8)
    private String customerState;
}
