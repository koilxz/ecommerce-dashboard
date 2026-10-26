package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.*;
import com.ecommerce.dashboard.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@Tag(name = "订单数据接口", description = "电商销售数据看板核心接口（对应需求 A1-A5），全部支持可选 sellerId 店铺切片")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/categories")
    @Operation(summary = "A1 · 获取商品品类列表", description = "返回所有可筛选的商品品类名称（中文）")
    public ApiResponse<List<String>> getCategories() {
        return ApiResponse.success(orderService.getCategoryList());
    }

    @GetMapping("/product-overview")
    @Operation(summary = "商品概览", description = "商品总数与品类数（商品表真实统计，供商品页 KPI 动态化）")
    public ApiResponse<ProductOverviewDTO> getProductOverview() {
        return ApiResponse.success(orderService.getProductOverview());
    }

    @GetMapping("/kpi")
    @Operation(summary = "A3 · 获取KPI汇总数据", description = "返回总销售额、订单数、客单价、商品数等核心指标及健康度字段")
    public ApiResponse<KpiDTO> getKpi(
            @Parameter(description = "品类名称，传all或不传表示全部") @RequestParam(required = false, defaultValue = "all") String category,
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选；不传或空表示全平台") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getKpi(category, startDate, endDate, blankToNull(sellerId)));
    }

    @GetMapping({"/trend", "/stats"})
    @Operation(summary = "A5 · 获取销售趋势", description = "按月/周/日聚合销售额与订单量。路径 /stats 为需求文档 A5 的别名，与 /trend 等价")
    public ApiResponse<TrendDTO> getTrend(
            @Parameter(description = "品类名称") @RequestParam(required = false, defaultValue = "all") String category,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "时间粒度：month/week/day") @RequestParam(required = false, defaultValue = "month") String granularity,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getTrend(category, startDate, endDate, granularity, blankToNull(sellerId)));
    }

    @GetMapping("/category-rank")
    @Operation(summary = "品类销售排名", description = "按销售额排序的品类排行")
    public ApiResponse<List<CategoryRankDTO>> getCategoryRank(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "返回数量") @RequestParam(required = false, defaultValue = "10") int limit,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getCategoryRank(startDate, endDate, limit, blankToNull(sellerId)));
    }

    @GetMapping({"/product-ranking", "/ranking"})
    @Operation(summary = "A4 · 商品销量TOP排名", description = "按销售件数或销售额排名的商品TOP N。路径 /ranking 为需求文档 A4 的别名，与 /product-ranking 等价")
    public ApiResponse<List<ProductRankDTO>> getProductRanking(
            @Parameter(description = "品类名称") @RequestParam(required = false, defaultValue = "all") String category,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "排序方式：count(按销量) / amount(按销售额)") @RequestParam(required = false, defaultValue = "count") String sortBy,
            @Parameter(description = "返回数量") @RequestParam(required = false, defaultValue = "10") int limit,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getProductRanking(category, startDate, endDate, sortBy, limit, blankToNull(sellerId)));
    }

    @GetMapping("/payment-distribution")
    @Operation(summary = "支付方式分布", description = "不同支付方式的订单占比")
    public ApiResponse<List<PaymentDistributionDTO>> getPaymentDistribution(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getPaymentDistribution(startDate, endDate, blankToNull(sellerId)));
    }

    @GetMapping("/user-distribution")
    @Operation(summary = "用户分布", description = "新老用户占比与高价值用户数")
    public ApiResponse<UserDistributionDTO> getUserDistribution(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getUserDistribution(startDate, endDate, blankToNull(sellerId)));
    }

    @GetMapping("")
    @Operation(summary = "A2 · 分页查询订单明细", description = "支持按品类、时间范围、关键字、卖家筛选，返回分页结果")
    public ApiResponse<PageResult<OrderDTO>> getOrders(
            @Parameter(description = "品类名称") @RequestParam(required = false, defaultValue = "all") String category,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "搜索关键词（商品名/订单号）") @RequestParam(required = false) String keyword,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") int pageNum,
            @Parameter(description = "每页条数") @RequestParam(required = false, defaultValue = "10") int pageSize,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(orderService.getOrders(category, startDate, endDate, keyword, pageNum, pageSize, blankToNull(sellerId)));
    }

    /** 空字符串统一转为 null，便于 Repository 的 :sid IS NULL 判断 */
    private String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
