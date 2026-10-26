package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.dto.LoginRequestDTO;
import com.ecommerce.dashboard.dto.LoginResponseDTO;
import com.ecommerce.dashboard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "登录接口", description = "轻量真实登录校验（BCrypt + 内存 token）")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "登录校验", description = "校验用户名/密码，成功返回 token")
    public ApiResponse<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO req) {
        try {
            return ApiResponse.success(authService.login(req));
        } catch (RuntimeException e) {
            return new ApiResponse<>(401, "用户名或密码错误", null);
        }
    }
}
