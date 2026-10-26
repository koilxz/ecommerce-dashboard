package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 客户 Repository（复购率：按 customer_unique_id 去重与回头客判定）
 */
public interface CustomerRepository extends JpaRepository<Customer, String> {

    /** 区间 + 卖家切片下的独立客户数（按 customer_unique_id） */
    @Query(value = "SELECT COUNT(DISTINCT c.customer_unique_id) FROM t_order o JOIN t_customer c ON o.customer_id=c.customer_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid))",
            nativeQuery = true)
    Long totalUniqueCustomers(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 回头客数（同一 customer_unique_id 在周期内下单 >= 2 次） */
    @Query(value = "SELECT COUNT(*) FROM (SELECT c.customer_unique_id FROM t_order o JOIN t_customer c ON o.customer_id=c.customer_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid)) " +
            "GROUP BY c.customer_unique_id HAVING COUNT(DISTINCT o.order_id) >= 2) t", nativeQuery = true)
    Long repeatCustomers(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 高价值客户数（同一 customer_unique_id 在周期内下单 >= 3 次） */
    @Query(value = "SELECT COUNT(*) FROM (SELECT c.customer_unique_id FROM t_order o JOIN t_customer c ON o.customer_id=c.customer_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid)) " +
            "GROUP BY c.customer_unique_id HAVING COUNT(DISTINCT o.order_id) >= 3) t", nativeQuery = true)
    Long countHighValueCustomers(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);
}
