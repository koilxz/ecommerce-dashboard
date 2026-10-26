package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.config.CategoryConstants;
import com.ecommerce.dashboard.config.RuleConfig;
import com.ecommerce.dashboard.dto.AlertDTO;
import com.ecommerce.dashboard.repository.OrderItemRepository;
import com.ecommerce.dashboard.service.AlertsService;
import com.ecommerce.dashboard.util.Periods;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 异常预警实现：
 *  - CATEGORY_DROP：品类销售额环比骤降 > alertCategoryDrop%（红）
 *  - SCORE_DROP：品类平均评分环比骤降 > alertScoreDrop（橙）
 *  - PAYMENT_SHIFT：支付方式占比突变 > alertPaymentShift 个百分点（橙）
 */
@Service
public class AlertsServiceImpl implements AlertsService {

    private final OrderItemRepository orderItemRepository;
    private final RuleConfig ruleConfig;

    public AlertsServiceImpl(OrderItemRepository orderItemRepository, RuleConfig ruleConfig) {
        this.orderItemRepository = orderItemRepository;
        this.ruleConfig = ruleConfig;
    }

    @Override
    public List<AlertDTO> alerts(String startDate, String endDate, String sellerId) {
        String[] p = Periods.resolve(startDate, endDate);
        String curStart = p[0], curEnd = p[1], prevStart = p[2], prevEnd = p[3];
        String sid = (sellerId == null || sellerId.isBlank()) ? null : sellerId.trim();

        Map<String, Double> curSales = toMap(orderItemRepository.categoryRank(curStart, curEnd, sid));
        Map<String, Double> prevSales = toMap(orderItemRepository.categoryRank(prevStart, prevEnd, sid));
        // 评分与评论数合并为一次聚合（categoryScoreStat），较拆成两条同链路查询每窗口少一次全表扫描
        Map<String, Double> curScore = new HashMap<>();
        Map<String, Long> curReviewCount = new HashMap<>();
        splitScoreStat(orderItemRepository.categoryScoreStat(curStart, curEnd, sid), curScore, curReviewCount);
        Map<String, Double> prevScore = new HashMap<>();
        Map<String, Long> prevReviewCount = new HashMap<>();
        splitScoreStat(orderItemRepository.categoryScoreStat(prevStart, prevEnd, sid), prevScore, prevReviewCount);
        List<Object[]> curPay = orderItemRepository.paymentDist(curStart, curEnd, sid);
        List<Object[]> prevPay = orderItemRepository.paymentDist(prevStart, prevEnd, sid);

        List<AlertDTO> alerts = new ArrayList<>();

        // 1) 品类销售额骤降
        double minSalesRaw = ruleConfig.getAlertCategoryDropMinSales() * 10000;  // 上期绝对额下限(原始 R$)
        for (Map.Entry<String, Double> e : curSales.entrySet()) {
            String en = e.getKey();
            double cs = e.getValue();
            double ps = prevSales.getOrDefault(en, 0.0);
            // 仅有意义的历史成交额才判定骤降，过滤「0.0万→x万」式小额品类噪声
            if (ps >= minSalesRaw) {
                double drop = (cs - ps) / ps * 100.0;
                if (drop <= -ruleConfig.getAlertCategoryDrop()) {
                    alerts.add(new AlertDTO(
                            "CATEGORY_DROP", "red",
                            "品类销售骤降",
                            "「" + CategoryConstants.categoryZh(en) + "」销售额环比 " + round1(drop) +
                                    "%（上期 " + round0(ps / 10000) + "万→本期 " + round0(cs / 10000) + "万 R$）",
                            CategoryConstants.categoryZh(en)));
                }
            }
        }

        // 2) 品类口碑下滑（加最小评论数保护：两期评论数均需达到阈值，避免「5.0→2.0」式小样本误报）
        int minReviews = ruleConfig.getAlertScoreDropMinReviews();
        for (Map.Entry<String, Double> e : curScore.entrySet()) {
            String en = e.getKey();
            double cs = e.getValue();
            double ps = prevScore.getOrDefault(en, 0.0);
            if (ps > 0) {
                double delta = cs - ps;
                long curN = curReviewCount.getOrDefault(en, 0L);
                long prevN = prevReviewCount.getOrDefault(en, 0L);
                if (delta <= -ruleConfig.getAlertScoreDrop() && curN >= minReviews && prevN >= minReviews) {
                    alerts.add(new AlertDTO(
                            "SCORE_DROP", "orange",
                            "品类口碑下滑",
                            "「" + CategoryConstants.categoryZh(en) + "」评分环比 " + round2(delta) +
                                    "（" + round2(ps) + "→" + round2(cs) + "，样本 " + prevN + "/" + curN + " 条）",
                            CategoryConstants.categoryZh(en)));
                }
            }
        }

        // 3) 支付方式占比突变
        Map<String, Double> curPayShare = payShare(curPay);
        Map<String, Double> prevPayShare = payShare(prevPay);
        for (Map.Entry<String, Double> e : curPayShare.entrySet()) {
            String type = e.getKey();
            double curPct = e.getValue();
            double prevPct = prevPayShare.getOrDefault(type, 0.0);
            if (Math.abs(curPct - prevPct) > ruleConfig.getAlertPaymentShift()) {
                alerts.add(new AlertDTO(
                        "PAYMENT_SHIFT", "orange",
                        "支付方式占比突变",
                        "「" + CategoryConstants.paymentZh(type) + "」占比 " + round1(prevPct) +
                                "%→" + round1(curPct) + "%（变动 " + round1(curPct - prevPct) + " 个百分点）",
                        ""));
            }
        }

        return alerts;
    }

    private static Map<String, Double> toMap(List<Object[]> rows) {
        Map<String, Double> m = new HashMap<>();
        if (rows == null) return m;
        for (Object[] r : rows) {
            if (r[0] == null) continue;
            String k = r[0].toString();
            double v = (r[1] instanceof Number) ? ((Number) r[1]).doubleValue() : 0.0;
            m.put(k, v);
        }
        return m;
    }

    /** 将 [品类, 平均评分, 评论条数] 聚合结果拆入两个 Map（一次扫描同时得到评分与样本数） */
    private static void splitScoreStat(List<Object[]> rows, Map<String, Double> scoreMap, Map<String, Long> countMap) {
        if (rows == null) return;
        for (Object[] r : rows) {
            if (r[0] == null) continue;
            String k = r[0].toString();
            double score = (r[1] instanceof Number) ? ((Number) r[1]).doubleValue() : 0.0;
            long n = (r[2] instanceof Number) ? ((Number) r[2]).longValue() : 0L;
            scoreMap.put(k, score);
            countMap.put(k, n);
        }
    }

    private static Map<String, Double> payShare(List<Object[]> rows) {
        Map<String, Double> share = new HashMap<>();
        if (rows == null) return share;
        double total = 0;
        for (Object[] r : rows) {
            if (r[1] instanceof Number) total += ((Number) r[1]).doubleValue();
        }
        if (total <= 0) return share;
        for (Object[] r : rows) {
            if (r[0] == null) continue;
            double cnt = (r[1] instanceof Number) ? ((Number) r[1]).doubleValue() : 0.0;
            share.put(r[0].toString(), cnt / total * 100.0);
        }
        return share;
    }

    private static double round0(double v) {
        return Math.round(v);
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
