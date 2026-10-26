package com.ecommerce.dashboard.config;

import com.ecommerce.dashboard.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/**
 * 登录拦截器：仅对 /diagnosis、/signals、/alerts 三个新接口校验请求头 X-Token。
 * 现有 /orders/** 保持公开，不破坏原有演示。
 * OPTIONS 预检直接放行（跨域）。
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Value("${app.auth.token-header:X-Token}")
    private String tokenHeader;

    private final AuthService authService;

    public LoginInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 跨域预检放行
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        boolean protectedApi = uri.contains("/diagnosis") || uri.contains("/signals") || uri.contains("/alerts");
        if (!protectedApi) {
            return true;
        }

        String token = request.getHeader(tokenHeader);
        if (token == null || !authService.validate(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"未登录或登录失效\"}");
            return false;
        }
        return true;
    }
}
