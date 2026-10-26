package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录响应 DTO（POST /api/auth/login 的 data）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO implements Serializable {
    private String token;        // 内存会话 token（UUID）
    private String username;     // 用户名
    private String displayName;  // 展示名
    private String role;         // 角色 SELLER / ADMIN
}
