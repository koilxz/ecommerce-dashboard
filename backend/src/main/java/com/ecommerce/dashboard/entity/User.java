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
 * 用户实体（对应 t_user，登录用，非 Olist 数据）
 * 密码以 BCrypt 哈希存储，演示账号由 AuthServiceImpl 启动时写入。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_user")
public class User implements Serializable {

    @Id
    @Column(name = "username", length = 32)
    private String username;

    @Column(name = "password", length = 64)
    private String password;

    @Column(name = "display_name", length = 64)
    private String displayName;

    @Column(name = "role", length = 20)
    private String role;
}
