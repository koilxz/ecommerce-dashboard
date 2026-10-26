package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.config.CategoryConstants;
import com.ecommerce.dashboard.dto.*;
import com.ecommerce.dashboard.entity.OrderItem;
import com.ecommerce.dashboard.entity.OrderItemId;
import com.ecommerce.dashboard.repository.*;
import com.ecommerce.dashboard.service.OrderService;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 真实查询实现（默认生效，覆盖 Mock）。
 * 所有金额在返回前 ÷10000 转为「万 R$」，品类/支付方式在 Service 层转中文，商品名合成。
 * 全部分析查询支持可选 sellerId 店铺切片（null = 全平台）。
 */
@Service
@Primary
public class JpaOrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public JpaOrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                               ReviewRepository reviewRepository, CustomerRepository customerRepository,
                               ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    // ===================== 公共工具 =====================

    /** 日期转查询下界（含当日 00:00:00） */
    private String startParam(String d) {
        return (d == null || d.isBlank()) ? null : d.trim() + " 00:00:00";
    }

    /** 日期转查询上界（含当日 23:59:59） */
    private String endParam(String d) {
        return (d == null || d.isBlank()) ? null : d.trim() + " 23:59:59";
    }

    /** 空 sellerId 统一为 null */
    private String sid(String sellerId) {
        return (sellerId == null || sellerId.isBlank()) ? null : sellerId.trim();
    }

    /** 中文品类 → 葡萄牙语存储值（all / 空 → null） */
    private String catPt(String category) {
        if (category == null || category.isBlank() || "all".equalsIgnoreCase(category)) {
            return null;
        }
        return CategoryConstants.zhToCategory(category);
    }

    private static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        return 0.0;
    }

    private static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return 0L;
    }

    /**
     * 环比增长率（%）：
     *  - 上一期为 0，或上一期销售额不足本期 1%（上期窗口基本落在数据集起始之前 / 样本极小）时返回 null，
     *    表示「无可比周期」；前端据此显示 --，避免把无比较对象渲染成 0.0% 或 +xxxx% 误导用户。
     *  - DTO 字段为包装类型 Double，null 可安全透传（消费方均已做空值处理）。
     */
    private Double growth(double cur, double prev) {
        if (prev <= 0 || prev < cur / 100.0) return null;
        return Math.round((cur - prev) / prev * 1000.0) / 10.0;
    }

    /** 根据起止日期推算上一等长周期（用于环比） */
    private String[] prevRange(String startDate, String endDate) {
        if (startDate == null || endDate == null || startDate.isBlank() || endDate.isBlank()) {
            return new String[]{null, null};
        }
        try {
            LocalDate s = LocalDate.parse(startDate.trim());
            LocalDate e = LocalDate.parse(endDate.trim());
            long days = ChronoUnit.DAYS.between(s, e) + 1;
            LocalDate prevEnd = s.minusDays(1);
            LocalDate prevStart = prevEnd.minusDays(days - 1);
            return new String[]{prevStart + " 00:00:00", prevEnd + " 23:59:59"};
        } catch (Exception ex) {
            return new String[]{null, null};
        }
    }

    // ===================== A1 品类列表 =====================

    @Override
    public List<String> getCategoryList() {
        List<String> ptList = productRepository.findExistingCategories();
        return ptList.stream()
                .map(CategoryConstants::categoryZh)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // ===================== 商品概览 =====================

    @Override
    public ProductOverviewDTO getProductOverview() {
        long total = productRepository.count();
        int categories = productRepository.findExistingCategories().size();
        return new ProductOverviewDTO(total, categories);
    }

    // ===================== A3 KPI =====================

    @Override
    public KpiDTO getKpi(String category, String startDate, String endDate, String sellerId) {
        String cat = catPt(category);
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        // 当前周期
        double salesR = (cat == null ? orderItemRepository.sumPrice(sp, ep, s)
                : orderItemRepository.sumPriceByCategory(cat, sp, ep, s)).doubleValue();
        long orders = cat == null ? orderItemRepository.countOrders(sp, ep, s)
                : orderItemRepository.countOrdersByCategory(cat, sp, ep, s);
        long products = cat == null ? orderItemRepository.countProducts(sp, ep, s)
                : orderItemRepository.countProductsByCategory(cat, sp, ep, s);
        double avgOrderValue = orders > 0 ? salesR / orders : 0.0;

        // 上一周期（环比）
        // ⚠️ 未传起止日期时不存在「上一等长周期」，此时 prevRange 返回 {null,null}，
        // 上一期查询会退化成「同一全量区间」，导致环比恒为 0.0（假数据）。
        // 因此无日期区间时环比一律返回 null，由前端渲染「--」，表示「无可比周期」。
        boolean hasRange = startDate != null && !startDate.isBlank()
                && endDate != null && !endDate.isBlank();
        String[] prev = prevRange(startDate, endDate);
        double prevSalesR = (cat == null ? orderItemRepository.sumPrice(prev[0], prev[1], s)
                : orderItemRepository.sumPriceByCategory(cat, prev[0], prev[1], s)).doubleValue();
        long prevOrders = cat == null ? orderItemRepository.countOrders(prev[0], prev[1], s)
                : orderItemRepository.countOrdersByCategory(cat, prev[0], prev[1], s);
        long prevProducts = cat == null ? orderItemRepository.countProducts(prev[0], prev[1], s)
                : orderItemRepository.countProductsByCategory(cat, prev[0], prev[1], s);
        double prevAvg = prevOrders > 0 ? prevSalesR / prevOrders : 0.0;

        double salesWan = Math.round(salesR / 10000.0 * 10.0) / 10.0;
        double avgOrderValueR = Math.round(avgOrderValue * 10.0) / 10.0;

        // 健康度字段（按 周期+卖家 计算，忽略品类筛选）
        Double avgScore = reviewRepository.avgScore(sp, ep, s);
        long totalUnique = customerRepository.totalUniqueCustomers(sp, ep, s);
        long repeat = customerRepository.repeatCustomers(sp, ep, s);
        long delivered = orderRepository.countDelivered(sp, ep, s);
        long delayed = orderRepository.countDelayed(sp, ep, s);
        long canceled = orderRepository.countCanceled(sp, ep, s);
        long totalOrdersAll = orderItemRepository.countOrders(sp, ep, s);
        long highValue = customerRepository.countHighValueCustomers(sp, ep, s);

        double repurchaseRate = totalUnique > 0 ? Math.round(repeat * 1000.0 / totalUnique) / 10.0 : 0.0;
        double delayRate = delivered > 0 ? Math.round(delayed * 1000.0 / delivered) / 10.0 : 0.0;
        double cancelRate = totalOrdersAll > 0 ? Math.round(canceled * 1000.0 / totalOrdersAll) / 10.0 : 0.0;

        return new KpiDTO(
                salesWan,
                (int) orders,
                avgOrderValueR,
                (int) products,
                hasRange ? growth(salesR, prevSalesR) : null,
                hasRange ? growth(orders, prevOrders) : null,
                hasRange ? growth(avgOrderValue, prevAvg) : null,
                hasRange ? growth(products, prevProducts) : null,
                repurchaseRate,
                avgScore == null ? 0.0 : Math.round(avgScore * 100.0) / 100.0,
                delayRate,
                cancelRate
        );
    }

    // ===================== A5 趋势 =====================

    @Override
    public TrendDTO getTrend(String category, String startDate, String endDate, String granularity, String sellerId) {
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        List<Object[]> daily = orderItemRepository.trendDaily(sp, ep, s);
        // key -> [salesR, orders]
        Map<String, double[]> buckets = new TreeMap<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        WeekFields wf = WeekFields.ISO;

        for (Object[] row : daily) {
            if (row[0] == null) continue;
            LocalDate date;
            if (row[0] instanceof java.sql.Date) {
                date = ((java.sql.Date) row[0]).toLocalDate();
            } else {
                date = LocalDate.parse(row[0].toString(), fmt);
            }
            double salesR = toDouble(row[1]);
            long orders = toLong(row[2]);

            String key;
            if ("day".equalsIgnoreCase(granularity)) {
                key = date.format(fmt);
            } else if ("week".equalsIgnoreCase(granularity)) {
                int week = date.get(wf.weekOfWeekBasedYear());
                key = date.getYear() + "-W" + String.format("%02d", week);
            } else { // month
                key = date.getYear() + "-" + String.format("%02d", date.getMonthValue());
            }
            double[] agg = buckets.computeIfAbsent(key, k -> new double[]{0, 0});
            agg[0] += salesR;
            agg[1] += orders;
        }

        List<String> labels = new ArrayList<>(buckets.keySet());
        List<Double> sales = new ArrayList<>();
        List<Integer> ordersList = new ArrayList<>();
        for (double[] agg : buckets.values()) {
            sales.add(Math.round(agg[0] / 10000.0 * 10.0) / 10.0);
            ordersList.add((int) agg[1]);
        }
        return new TrendDTO(labels, sales, ordersList);
    }

    // ===================== 品类排行 =====================

    @Override
    public List<CategoryRankDTO> getCategoryRank(String startDate, String endDate, int limit, String sellerId) {
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        List<Object[]> rows = orderItemRepository.categoryRank(sp, ep, s);
        List<CategoryRankDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            String pt = r[0] == null ? "" : r[0].toString();
            double salesWan = Math.round(toDouble(r[1]) / 10000.0 * 10.0) / 10.0;
            int ord = (int) toLong(r[2]);
            list.add(new CategoryRankDTO(CategoryConstants.categoryZh(pt), pt, salesWan, ord));
        }
        list.sort((a, b) -> Double.compare(b.getSalesAmount(), a.getSalesAmount()));
        if (limit > 0 && list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list;
    }

    // ===================== A4 商品排行 =====================

    @Override
    public List<ProductRankDTO> getProductRanking(String category, String startDate, String endDate,
                                                  String sortBy, int limit, String sellerId) {
        String cat = catPt(category);
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        List<Object[]> rows = orderItemRepository.productRank(cat, sp, ep, s);
        List<ProductRankDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            String productId = r[0] == null ? "" : r[0].toString();
            String pt = r[1] == null ? "" : r[1].toString();
            double sumPrice = toDouble(r[2]);
            long count = toLong(r[3]);
            double price = count > 0 ? sumPrice / count : 0.0;
            list.add(new ProductRankDTO(
                    productId,
                    CategoryConstants.productName(CategoryConstants.categoryZh(pt), productId),
                    CategoryConstants.categoryZh(pt),
                    Math.round(price * 100.0) / 100.0,
                    (int) count,
                    Math.round(sumPrice / 10000.0 * 10.0) / 10.0
            ));
        }
        if ("count".equalsIgnoreCase(sortBy)) {
            list.sort((a, b) -> Integer.compare(b.getSalesCount(), a.getSalesCount()));
        } else {
            list.sort((a, b) -> Double.compare(b.getSalesAmount(), a.getSalesAmount()));
        }
        if (limit > 0 && list.size() > limit) {
            list = list.subList(0, limit);
        }
        return list;
    }

    // ===================== 支付分布 =====================

    @Override
    public List<PaymentDistributionDTO> getPaymentDistribution(String startDate, String endDate, String sellerId) {
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        List<Object[]> rows = orderItemRepository.paymentDist(sp, ep, s);
        long total = rows.stream().mapToLong(r -> toLong(r[1])).sum();
        List<PaymentDistributionDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            String type = r[0] == null ? "" : r[0].toString();
            long cnt = toLong(r[1]);
            double pct = total > 0 ? Math.round(cnt * 1000.0 / total) / 10.0 : 0.0;
            list.add(new PaymentDistributionDTO(CategoryConstants.paymentZh(type), (int) cnt, pct));
        }
        return list;
    }

    // ===================== 用户分布 =====================

    @Override
    public UserDistributionDTO getUserDistribution(String startDate, String endDate, String sellerId) {
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);

        long totalUnique = customerRepository.totalUniqueCustomers(sp, ep, s);
        long repeat = customerRepository.repeatCustomers(sp, ep, s);
        long newUser = Math.max(0, totalUnique - repeat);
        long highValue = customerRepository.countHighValueCustomers(sp, ep, s);

        double newPct = totalUnique > 0 ? Math.round(newUser * 1000.0 / totalUnique) / 10.0 : 0.0;
        double repeatPct = totalUnique > 0 ? Math.round(repeat * 1000.0 / totalUnique) / 10.0 : 0.0;
        double highPct = totalUnique > 0 ? Math.round(highValue * 1000.0 / totalUnique) / 10.0 : 0.0;

        return new UserDistributionDTO((int) newUser, (int) repeat, newPct, repeatPct, (int) highValue, highPct);
    }

    // ===================== A2 订单分页 =====================

    @Override
    public PageResult<OrderDTO> getOrders(String category, String startDate, String endDate,
                                          String keyword, int pageNum, int pageSize, String sellerId) {
        String cat = catPt(category);
        String s = sid(sellerId);
        String sp = startParam(startDate);
        String ep = endParam(endDate);
        String kwLike = (keyword == null || keyword.isBlank()) ? null : "%" + keyword.trim() + "%";
        Pageable pageable = PageRequest.of(Math.max(0, pageNum - 1), Math.max(1, pageSize));

        List<Object[]> rows = orderRepository.findOrdersPaged(cat, sp, ep, s, kwLike, pageable);
        long total = orderRepository.countOrdersFiltered(cat, sp, ep, s, kwLike);

        List<OrderDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            String orderId = r[0] == null ? "" : r[0].toString();
            String productId = r[1] == null ? "" : r[1].toString();
            String pt = r[2] == null ? "" : r[2].toString();
            BigDecimal price = r[3] instanceof BigDecimal ? (BigDecimal) r[3] : BigDecimal.valueOf(toDouble(r[3]));
            LocalDateTime purchaseTime = r[5] instanceof java.sql.Timestamp
                    ? ((java.sql.Timestamp) r[5]).toLocalDateTime() : null;
            String paymentType = r[6] == null ? "" : r[6].toString();
            String orderStatus = r[7] == null ? "" : r[7].toString();

            list.add(new OrderDTO(
                    orderId,
                    CategoryConstants.productName(CategoryConstants.categoryZh(pt), productId),
                    CategoryConstants.categoryZh(pt),
                    price,
                    1,
                    price,
                    purchaseTime,
                    CategoryConstants.paymentZh(paymentType),
                    orderStatus
            ));
        }
        return new PageResult<>(list, total, pageNum, pageSize);
    }
}
