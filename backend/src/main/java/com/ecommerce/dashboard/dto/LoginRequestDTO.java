package com.ecommerce.dashboard.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 登录请求 DTO（POST /api/auth/login）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO implements Serializable {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}
