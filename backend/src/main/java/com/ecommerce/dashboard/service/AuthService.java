package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.LoginRequestDTO;
import com.ecommerce.dashboard.dto.LoginResponseDTO;

/**
 * 登录服务接口（轻量真实校验：BCrypt + 内存 token）
 */
public interface AuthService {

    /** 登录校验，成功返回 token 等信息；失败抛出 RuntimeException */
    LoginResponseDTO login(LoginRequestDTO req);

    /** 校验 token 是否有效（受保护接口拦截器调用） */
    boolean validate(String token);

    /** 由 token 反查用户名 */
    String getUsername(String token);
}
