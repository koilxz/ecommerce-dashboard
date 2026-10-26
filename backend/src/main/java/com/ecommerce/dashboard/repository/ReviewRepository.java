package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 评价 Repository（口碑/评分维度，及信号/预警的品类评分）
 */
public interface ReviewRepository extends JpaRepository<Review, String> {

    /** 区间 + 卖家切片下的平均评分 */
    @Query(value = "SELECT COALESCE(AVG(r.review_score),0) FROM t_review r JOIN t_order o ON r.order_id=o.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid))",
            nativeQuery = true)
    Double avgScore(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 品类平均评分：[英文品类名, 平均评分] */
    @Query(value = "SELECT p.category_name, COALESCE(AVG(r.review_score),0) FROM t_review r " +
            "JOIN t_order o ON r.order_id=o.order_id JOIN t_order_item oi ON oi.order_id=o.order_id " +
            "LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) GROUP BY p.category_name", nativeQuery = true)
    List<Object[]> categoryAvgScore(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);
}
