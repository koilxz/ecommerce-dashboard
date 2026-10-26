package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.dto.LoginRequestDTO;
import com.ecommerce.dashboard.dto.LoginResponseDTO;
import com.ecommerce.dashboard.entity.User;
import com.ecommerce.dashboard.repository.UserRepository;
import com.ecommerce.dashboard.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 登录服务实现（轻量真实校验）：
 *  - 口令以 BCrypt 哈希存储，登录时比对；
 *  - 校验通过生成 UUID token，存入内存 Map<token, username>（应用级会话，无需 Redis）；
 *  - 演示账号（demo/demo123、admin/admin123）由 DatabaseInitializer 在建表后播种，避免与建表顺序冲突。
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /** 内存 token 表：token -> username */
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    @Autowired
    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO req) {
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (opt.isEmpty() || !encoder.matches(req.getPassword(), opt.get().getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        User u = opt.get();
        String token = UUID.randomUUID().toString();
        tokenMap.put(token, u.getUsername());
        return new LoginResponseDTO(token, u.getUsername(), u.getDisplayName(), u.getRole());
    }

    @Override
    public boolean validate(String token) {
        return token != null && tokenMap.containsKey(token);
    }

    @Override
    public String getUsername(String token) {
        return token == null ? null : tokenMap.get(token);
    }
}
