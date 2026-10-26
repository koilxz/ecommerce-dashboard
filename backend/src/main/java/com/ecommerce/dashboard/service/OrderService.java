package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.*;

import java.util.List;

/**
 * 订单服务接口
 * 注：v2 增量在原有方法上统一追加可选 sellerId 参数（店铺切片，null = 全平台），Controller 同步透传。
 */
public interface OrderService {

    /**
     * A1: 获取品类列表（全局，不随卖家切片）
     */
    List<String> getCategoryList();

    /**
     * 商品概览：商品总数与品类数（商品表真实统计，供商品页 KPI 动态化）
     */
    ProductOverviewDTO getProductOverview();

    /**
     * A3: 获取KPI汇总数据
     */
    KpiDTO getKpi(String category, String startDate, String endDate, String sellerId);

    /**
     * A5: 获取销售趋势数据
     */
    TrendDTO getTrend(String category, String startDate, String endDate, String granularity, String sellerId);

    /**
     * 品类销售排名
     */
    List<CategoryRankDTO> getCategoryRank(String startDate, String endDate, int limit, String sellerId);

    /**
     * A4: 商品销量TOP10
     */
    List<ProductRankDTO> getProductRanking(String category, String startDate, String endDate, String sortBy, int limit, String sellerId);

    /**
     * 支付方式分布
     */
    List<PaymentDistributionDTO> getPaymentDistribution(String startDate, String endDate, String sellerId);

    /**
     * 新老用户占比
     */
    UserDistributionDTO getUserDistribution(String startDate, String endDate, String sellerId);

    /**
     * A2: 分页查询订单
     */
    PageResult<OrderDTO> getOrders(String category, String startDate, String endDate,
                                   String keyword, int pageNum, int pageSize, String sellerId);
}
