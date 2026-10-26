package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.dto.*;
import com.ecommerce.dashboard.service.OrderService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Mock 订单服务实现（基于 Olist 真实数据集统计的模拟数据，仅作参考保留）。
 * 仅当 spring.profiles.active=mock 时启用；默认由 JpaOrderServiceImpl(@Primary) 覆盖。
 */
@Profile("mock")
@Service
public class MockOrderServiceImpl implements OrderService {

    private static final List<String> CATEGORIES_ZH = Arrays.asList(
            "健康美容", "手表礼品", "床品卫浴", "运动休闲", "电脑配件",
            "家具装饰", "酷玩", "家居用品", "汽车", "园艺工具",
            "玩具", "办公用品", "服装", "美食", "图书"
    );

    private static final List<String> CATEGORIES_EN = Arrays.asList(
            "health_beauty", "watches_gifts", "bed_bath_table", "sports_leisure", "computers_accessories",
            "furniture_decor", "cool_stuff", "housewares", "auto", "garden_tools",
            "toys", "office_furniture", "fashion", "food", "books"
    );

    private static final double[] CAT_SALES = {125.9, 120.5, 103.7, 98.8, 91.2, 73.0, 63.5, 63.2, 59.3, 48.5, 41.2, 35.6, 32.1, 28.5, 22.3};
    private static final int[] CAT_ORDERS = {9670, 5991, 11115, 8641, 7827, 8334, 3797, 6964, 4235, 1250, 4135, 2856, 3542, 2134, 1876};

    private static final String[] PAYMENT_TYPES = {"信用卡", "Boleto", "代金券", "借记卡"};
    private static final int[] PAYMENT_COUNTS = {76795, 19784, 5775, 1529};

    private static final String[] PRODUCT_NAMES = {
            "氨基酸洁面乳", "智能运动手表", "浴巾三件套", "瑜伽垫套装", "无线蓝牙耳机",
            "北欧风台灯", "桌面收纳盒", "汽车香薰", "益智积木玩具", "园艺工具套装",
            "复古太阳镜", "精华液套装", "真皮钱包", "咖啡机", "无人机",
            "保温杯", "滑板", "电动牙刷", "香水礼盒", "机械键盘"
    };

    private static final String[] ORDER_STATUSES = {"delivered", "delivered", "delivered", "delivered", "shipped", "canceled"};

    @Override
    public List<String> getCategoryList() {
        return new ArrayList<>(CATEGORIES_ZH);
    }

    @Override
    public ProductOverviewDTO getProductOverview() {
        return new ProductOverviewDTO(32951L, CATEGORIES_ZH.size());
    }

    @Override
    public KpiDTO getKpi(String category, String startDate, String endDate, String sellerId) {
        double baseSales = 1359.0;
        int baseOrders = 99441;
        double baseAvg = 136.7;
        int baseProducts = 32951;

        // 如果指定了品类，按比例缩小
        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            int idx = CATEGORIES_ZH.indexOf(category);
            if (idx >= 0 && idx < CAT_SALES.length) {
                baseSales = CAT_SALES[idx];
                baseOrders = CAT_ORDERS[idx];
                baseAvg = baseSales * 10000 / baseOrders;
                baseProducts = (int) (32951 * CAT_ORDERS[idx] / 99441.0);
            }
        }

        return new KpiDTO(
                Math.round(baseSales * 10) / 10.0,
                baseOrders,
                Math.round(baseAvg * 10) / 10.0,
                baseProducts,
                12.8, 8.5, 3.2, 5.6,
                6.6, 4.1, 8.0, 2.3
        );
    }

    @Override
    public TrendDTO getTrend(String category, String startDate, String endDate, String granularity, String sellerId) {
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月",
                "次年1月", "次年2月", "次年3月", "次年4月", "次年5月", "次年6月", "次年7月", "次年8月"};
        double[] sales = {12.0, 24.7, 37.4, 36.0, 50.6, 43.3, 49.8, 57.4, 62.4, 66.4, 101.0, 74.4,
                95.0, 84.4, 98.3, 99.7, 99.7, 86.5, 89.6, 85.5};
        int[] orders = {955, 1951, 3000, 2684, 4136, 3583, 4519, 4910, 4831, 5322, 8665, 6308,
                8208, 7672, 8217, 7975, 7925, 7078, 7092, 7248};

        double ratio = 1.0;
        if (category != null && !category.isEmpty() && !"all".equals(category)) {
            int idx = CATEGORIES_ZH.indexOf(category);
            if (idx >= 0 && idx < CAT_SALES.length) {
                ratio = CAT_SALES[idx] / 1359.0;
            }
        }

        List<Double> salesList = new ArrayList<>();
        List<Integer> ordersList = new ArrayList<>();
        for (int i = 0; i < sales.length; i++) {
            salesList.add(Math.round(sales[i] * ratio * 10) / 10.0);
            ordersList.add((int) (orders[i] * ratio));
        }

        return new TrendDTO(Arrays.asList(months), salesList, ordersList);
    }

    @Override
    public List<CategoryRankDTO> getCategoryRank(String startDate, String endDate, int limit, String sellerId) {
        List<CategoryRankDTO> list = new ArrayList<>();
        int count = Math.min(limit, CATEGORIES_ZH.size());
        for (int i = 0; i < count; i++) {
            list.add(new CategoryRankDTO(
                    CATEGORIES_ZH.get(i),
                    CATEGORIES_EN.get(i),
                    CAT_SALES[i],
                    CAT_ORDERS[i]
            ));
        }
        list.sort((a, b) -> Double.compare(b.getSalesAmount(), a.getSalesAmount()));
        return list;
    }

    @Override
    public List<ProductRankDTO> getProductRanking(String category, String startDate, String endDate, String sortBy, int limit, String sellerId) {
        List<ProductRankDTO> list = new ArrayList<>();
        Random rand = new Random(42);
        for (int i = 0; i < Math.min(limit, PRODUCT_NAMES.length); i++) {
            double price = 30 + rand.nextDouble() * 270;
            int count = 200 + rand.nextInt(9800);
            list.add(new ProductRankDTO(
                    "PROD" + String.format("%05d", i + 1),
                    PRODUCT_NAMES[i],
                    CATEGORIES_ZH.get(i % CATEGORIES_ZH.size()),
                    Math.round(price * 100) / 100.0,
                    count,
                    Math.round(price * count) / 100.0
            ));
        }
        if ("sales".equals(sortBy)) {
            list.sort((a, b) -> Integer.compare(b.getSalesCount(), a.getSalesCount()));
        } else {
            list.sort((a, b) -> Double.compare(b.getSalesAmount(), a.getSalesAmount()));
        }
        return list;
    }

    @Override
    public List<PaymentDistributionDTO> getPaymentDistribution(String startDate, String endDate, String sellerId) {
        List<PaymentDistributionDTO> list = new ArrayList<>();
        int total = Arrays.stream(PAYMENT_COUNTS).sum();
        for (int i = 0; i < PAYMENT_TYPES.length; i++) {
            list.add(new PaymentDistributionDTO(
                    PAYMENT_TYPES[i],
                    PAYMENT_COUNTS[i],
                    Math.round(PAYMENT_COUNTS[i] * 1000.0 / total) / 10.0
            ));
        }
        return list;
    }

    @Override
    public UserDistributionDTO getUserDistribution(String startDate, String endDate, String sellerId) {
        int total = 96096;
        int repeat = (int) (total * 0.066);
        int newUser = total - repeat;
        int highValue = (int) (total * 0.021);
        return new UserDistributionDTO(
                newUser, repeat,
                Math.round(newUser * 1000.0 / total) / 10.0,
                Math.round(repeat * 1000.0 / total) / 10.0,
                highValue,
                Math.round(highValue * 1000.0 / total) / 10.0
        );
    }

    @Override
    public PageResult<OrderDTO> getOrders(String category, String startDate, String endDate,
                                          String keyword, int pageNum, int pageSize, String sellerId) {
        List<OrderDTO> allOrders = generateMockOrders(200);

        List<OrderDTO> filtered = allOrders.stream()
                .filter(o -> category == null || "all".equals(category) || category.isEmpty()
                        || o.getCategory().equals(category))
                .filter(o -> keyword == null || keyword.isEmpty()
                        || o.getProductName().contains(keyword)
                        || o.getOrderId().contains(keyword))
                .collect(Collectors.toList());

        int total = filtered.size();
        int from = Math.min((pageNum - 1) * pageSize, total);
        int to = Math.min(from + pageSize, total);
        List<OrderDTO> pageList = filtered.subList(from, to);

        return new PageResult<>(new ArrayList<>(pageList), total, pageNum, pageSize);
    }

    private List<OrderDTO> generateMockOrders(int count) {
        Random rand = new Random(12345);
        List<OrderDTO> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            int catIdx = rand.nextInt(CATEGORIES_ZH.size());
            double price = 20 + rand.nextDouble() * 300;
            int qty = 1 + rand.nextInt(4);
            list.add(new OrderDTO(
                    generateOrderId(rand),
                    PRODUCT_NAMES[i % PRODUCT_NAMES.length],
                    CATEGORIES_ZH.get(catIdx),
                    BigDecimal.valueOf(Math.round(price * 100) / 100.0),
                    qty,
                    BigDecimal.valueOf(Math.round(price * qty * 100) / 100.0),
                    LocalDateTime.of(
                            2017 + rand.nextInt(2),
                            1 + rand.nextInt(12),
                            1 + rand.nextInt(27),
                            rand.nextInt(24),
                            rand.nextInt(60)
                    ),
                    PAYMENT_TYPES[rand.nextInt(PAYMENT_TYPES.length)],
                    ORDER_STATUSES[rand.nextInt(ORDER_STATUSES.length)]
            ));
        }
        list.sort((a, b) -> b.getPurchaseTime().compareTo(a.getPurchaseTime()));
        return list;
    }

    private String generateOrderId(Random rand) {
        String chars = "abcdef0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            sb.append(chars.charAt(rand.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
