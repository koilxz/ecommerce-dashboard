package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.OrderItem;
import com.ecommerce.dashboard.entity.OrderItemId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 订单明细 Repository（事实表，承载大部分聚合与店铺切片查询）
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, OrderItemId> {

    /** 区间 + 卖家切片下的销售额合计（R$） */
    @Query(value = "SELECT COALESCE(SUM(oi.price),0) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid)", nativeQuery = true)
    java.math.BigDecimal sumPrice(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 区间 + 卖家切片下的订单数（去重 order_id） */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid)", nativeQuery = true)
    Long countOrders(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 区间 + 卖家切片下的商品数（去重 product_id） */
    @Query(value = "SELECT COUNT(DISTINCT oi.product_id) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid)", nativeQuery = true)
    Long countProducts(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 按品类过滤的销售额合计（join product） */
    @Query(value = "SELECT COALESCE(SUM(oi.price),0) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) AND (:cat IS NULL OR p.category_name=:cat)", nativeQuery = true)
    java.math.BigDecimal sumPriceByCategory(@Param("cat") String categoryEn, @Param("start") String start,
                                            @Param("end") String end, @Param("sid") String sellerId);

    /** 按品类过滤的订单数 */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) AND (:cat IS NULL OR p.category_name=:cat)", nativeQuery = true)
    Long countOrdersByCategory(@Param("cat") String categoryEn, @Param("start") String start,
                               @Param("end") String end, @Param("sid") String sellerId);

    /** 按品类过滤的商品数 */
    @Query(value = "SELECT COUNT(DISTINCT oi.product_id) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) AND (:cat IS NULL OR p.category_name=:cat)", nativeQuery = true)
    Long countProductsByCategory(@Param("cat") String categoryEn, @Param("start") String start,
                                 @Param("end") String end, @Param("sid") String sellerId);

    /** 趋势：按天聚合销售额与订单数（服务层再按 月/周/日 分桶） */
    @Query(value = "SELECT CAST(o.purchase_timestamp AS DATE), COALESCE(SUM(oi.price),0), COUNT(DISTINCT o.order_id) " +
            "FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) GROUP BY CAST(o.purchase_timestamp AS DATE) " +
            "ORDER BY CAST(o.purchase_timestamp AS DATE)", nativeQuery = true)
    List<Object[]> trendDaily(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 品类销售排行：[英文品类名, 销售额R$, 订单数] */
    @Query(value = "SELECT p.category_name, COALESCE(SUM(oi.price),0), COUNT(DISTINCT o.order_id) " +
            "FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) GROUP BY p.category_name ORDER BY SUM(oi.price) DESC", nativeQuery = true)
    List<Object[]> categoryRank(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 商品销量排行：[product_id, 英文品类名, 销售额R$, 销售件数] */
    @Query(value = "SELECT oi.product_id, p.category_name, COALESCE(SUM(oi.price),0), COUNT(*) " +
            "FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) AND (:cat IS NULL OR p.category_name=:cat) " +
            "GROUP BY oi.product_id, p.category_name ORDER BY SUM(oi.price) DESC", nativeQuery = true)
    List<Object[]> productRank(@Param("cat") String categoryEn, @Param("start") String start,
                               @Param("end") String end, @Param("sid") String sellerId);

    /** 支付方式分布：[payment_type, 笔数] */
    @Query(value = "SELECT pay.payment_type, COUNT(*) FROM t_order o JOIN t_order_item oi ON o.order_id=oi.order_id " +
            "JOIN t_payment pay ON pay.order_id=o.order_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) GROUP BY pay.payment_type", nativeQuery = true)
    List<Object[]> paymentDist(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 品类评分统计：[英文品类名, 平均评分, 评论条数]（review→order→order_item→product，近似按订单评分归属品类；
     *  平均评分与评论数一次扫描同时产出，供评分环比预警做小样本保护，避免同链路重复全表聚合） */
    @Query(value = "SELECT p.category_name, COALESCE(AVG(r.review_score),0), COUNT(r.review_id) " +
            "FROM t_review r JOIN t_order o ON r.order_id=o.order_id " +
            "JOIN t_order_item oi ON o.order_id=oi.order_id LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) GROUP BY p.category_name", nativeQuery = true)
    List<Object[]> categoryScoreStat(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);
}
