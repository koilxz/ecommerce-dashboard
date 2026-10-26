package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * 用户 Repository（登录校验）
 */
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    /** 是否已存在任意演示账号（用于启动时只写入一次） */
    @Query("SELECT COUNT(u) > 0 FROM User u")
    boolean existsAny();
}
