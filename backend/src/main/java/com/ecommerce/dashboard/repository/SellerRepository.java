package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 卖家 Repository（店铺代理，提供预设演示店铺 top5）
 */
public interface SellerRepository extends JpaRepository<Seller, String> {

    /** 按销量（去重订单数）取前 N 名卖家作为演示店铺：[seller_id, seller_city, seller_state, 订单数] */
    @Query(value = "SELECT oi.seller_id, s.seller_city, s.seller_state, COUNT(DISTINCT oi.order_id) " +
            "FROM t_order_item oi LEFT JOIN t_seller s ON oi.seller_id=s.seller_id " +
            "GROUP BY oi.seller_id, s.seller_city, s.seller_state " +
            "ORDER BY COUNT(DISTINCT oi.order_id) DESC LIMIT 5", nativeQuery = true)
    List<Object[]> topSellers();
}
