package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Payment;
import com.ecommerce.dashboard.entity.PaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 支付 Repository（聚合查询在 OrderItemRepository 中 join 完成）
 */
public interface PaymentRepository extends JpaRepository<Payment, PaymentId> {
}
