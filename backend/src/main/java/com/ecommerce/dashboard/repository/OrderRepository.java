package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 订单 Repository（主表，承载退单/延误/分页查询）
 */
public interface OrderRepository extends JpaRepository<Order, String> {

    /** 退单（canceled）数量 */
    @Query(value = "SELECT COUNT(*) FROM t_order o " +
            "WHERE o.order_status='canceled' " +
            "AND (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid))",
            nativeQuery = true)
    Long countCanceled(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 已送达且实际送达晚于预计送达（物流延误）的数量 */
    @Query(value = "SELECT COUNT(*) FROM t_order o " +
            "WHERE o.delivered_customer_date IS NOT NULL AND o.estimated_delivery_date IS NOT NULL " +
            "AND o.delivered_customer_date > o.estimated_delivery_date " +
            "AND (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid))",
            nativeQuery = true)
    Long countDelayed(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 已送达订单数（延误率分母） */
    @Query(value = "SELECT COUNT(*) FROM t_order o " +
            "WHERE o.delivered_customer_date IS NOT NULL " +
            "AND (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi WHERE oi.order_id=o.order_id AND oi.seller_id=:sid))",
            nativeQuery = true)
    Long countDelivered(@Param("start") String start, @Param("end") String end, @Param("sid") String sellerId);

    /** 订单明细分页（每个订单取首个商品作为代表行）[order_id, product_id, 英文品类, price, freight, 下单时间, payment_type, order_status] */
    @Query(value = "SELECT o.order_id, oi.product_id, p.category_name, oi.price, oi.freight_value, o.purchase_timestamp, pay.payment_type, o.order_status " +
            "FROM t_order o " +
            "JOIN (SELECT order_id, MIN(order_item_id) AS mid FROM t_order_item GROUP BY order_id) f ON o.order_id=f.order_id " +
            "JOIN t_order_item oi ON oi.order_id=f.order_id AND oi.order_item_id=f.mid " +
            "LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "LEFT JOIN (SELECT order_id, MIN(payment_type) AS payment_type FROM t_payment GROUP BY order_id) pay ON pay.order_id=o.order_id " +
            "WHERE (:cat IS NULL OR p.category_name=:cat) " +
            "AND (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR EXISTS(SELECT 1 FROM t_order_item oi2 WHERE oi2.order_id=o.order_id AND oi2.seller_id=:sid)) " +
            "AND (:kwLike IS NULL OR o.order_id LIKE :kwLike OR oi.product_id LIKE :kwLike) " +
            "ORDER BY o.purchase_timestamp DESC", nativeQuery = true)
    List<Object[]> findOrdersPaged(@Param("cat") String categoryEn, @Param("start") String start, @Param("end") String end,
                                   @Param("sid") String sellerId, @Param("kwLike") String kwLike, Pageable pageable);

    /** 订单明细总数（分页用） */
    @Query(value = "SELECT COUNT(DISTINCT o.order_id) FROM t_order o " +
            "JOIN t_order_item oi ON o.order_id=oi.order_id LEFT JOIN t_product p ON oi.product_id=p.product_id " +
            "WHERE (:cat IS NULL OR p.category_name=:cat) " +
            "AND (:start IS NULL OR o.purchase_timestamp >= :start) AND (:end IS NULL OR o.purchase_timestamp <= :end) " +
            "AND (:sid IS NULL OR oi.seller_id=:sid) " +
            "AND (:kwLike IS NULL OR o.order_id LIKE :kwLike OR oi.product_id LIKE :kwLike)", nativeQuery = true)
    Long countOrdersFiltered(@Param("cat") String categoryEn, @Param("start") String start, @Param("end") String end,
                             @Param("sid") String sellerId, @Param("kwLike") String kwLike);
}
