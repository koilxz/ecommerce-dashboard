package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.config.CategoryConstants;
import com.ecommerce.dashboard.config.RuleConfig;
import com.ecommerce.dashboard.dto.*;
import com.ecommerce.dashboard.repository.OrderItemRepository;
import com.ecommerce.dashboard.service.DiagnosisService;
import com.ecommerce.dashboard.service.OrderService;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 经营健康度诊断实现（规则引擎）：
 *  - 阈值来自 RuleConfig（可配置）；
 *  - 逐指标比对生成 SignalDTO[]；
 *  - 按模板拼装 conclusionText 人话结论（前端只渲染）。
 */
@Service
public class DiagnosisServiceImpl implements DiagnosisService {

    private final OrderService orderService;
    private final OrderItemRepository orderItemRepository;
    private final RuleConfig ruleConfig;

    public DiagnosisServiceImpl(OrderService orderService, OrderItemRepository orderItemRepository, RuleConfig ruleConfig) {
        this.orderService = orderService;
        this.orderItemRepository = orderItemRepository;
        this.ruleConfig = ruleConfig;
    }

    @Override
    public DiagnosisDTO diagnose(String startDate, String endDate, String sellerId) {
        // 默认窗口取数据集尾部 6 个月（与 signals/alerts 一致），避免「上期近乎空」导致环比失真
        String start = (startDate == null || startDate.isBlank()) ? "2018-03-01" : startDate.trim();
        String end = (endDate == null || endDate.isBlank()) ? "2018-09-30" : endDate.trim();
        String s = (sellerId == null || sellerId.isBlank()) ? null : sellerId.trim();
        String sp = start + " 00:00:00";
        String ep = end + " 23:59:59";

        // 核心 KPI（含健康度字段）
        KpiDTO kpi = orderService.getKpi("all", start, end, s);
        double totalSales = kpi.getTotalSales() == null ? 0 : kpi.getTotalSales();
        double salesGrowth = kpi.getSalesGrowth() == null ? 0 : kpi.getSalesGrowth();
        double repurchaseRate = kpi.getRepurchaseRate() == null ? 0 : kpi.getRepurchaseRate();
        double avgReviewScore = kpi.getAvgReviewScore() == null ? 0 : kpi.getAvgReviewScore();
        double delayRate = kpi.getDelayRate() == null ? 0 : kpi.getDelayRate();
        double cancelRate = kpi.getCancelRate() == null ? 0 : kpi.getCancelRate();

        // 信号生成
        List<SignalDTO> signals = new ArrayList<>();
        buildSalesSignal(signals, salesGrowth);
        buildRepurchaseSignal(signals, repurchaseRate);
        buildReviewSignal(signals, avgReviewScore);
        buildDelaySignal(signals, delayRate);
        buildCancelSignal(signals, cancelRate);

        // 主力品类（用于结论文案）
        String topCategory = topCategory(sp, ep, s);

        // 结论文案
        String conclusion = buildConclusion(totalSales, salesGrowth, topCategory, repurchaseRate, delayRate, cancelRate);

        DiagnosisDTO.DiagnosisPeriod period = new DiagnosisDTO.DiagnosisPeriod(start, end, "所选周期");
        DiagnosisDTO.DiagnosisKpi kpiView = new DiagnosisDTO.DiagnosisKpi(
                round1(totalSales), round1(salesGrowth), round1(repurchaseRate),
                round2(avgReviewScore), round1(delayRate), round1(cancelRate));

        return new DiagnosisDTO(period, kpiView, signals, conclusion);
    }

    // ===================== 信号 =====================

    private void buildSalesSignal(List<SignalDTO> signals, double g) {
        if (g > ruleConfig.getSalesGrowthStrong()) {
            signals.add(new SignalDTO("SALES_GROWTH_STRONG", "green", "增长强劲",
                    "销售额环比", "+" + round1(g) + "%", ">+" + ruleConfig.getSalesGrowthStrong() + "%"));
        } else if (g < ruleConfig.getSalesGrowthDecline()) {
            signals.add(new SignalDTO("SALES_GROWTH_DECLINE", "red", "下滑预警",
                    "销售额环比", round1(g) + "%", "<" + ruleConfig.getSalesGrowthDecline() + "%"));
        } else {
            signals.add(new SignalDTO("SALES_GROWTH_FLAT", "gray", "平稳",
                    "销售额环比", "+" + round1(g) + "%", ruleConfig.getSalesGrowthDecline() + "%~+" + ruleConfig.getSalesGrowthStrong() + "%"));
        }
    }

    private void buildRepurchaseSignal(List<SignalDTO> signals, double r) {
        if (r < ruleConfig.getRepurchaseLow()) {
            signals.add(new SignalDTO("REPAIR_LOW", "orange", "拉新不足",
                    "复购率", round1(r) + "%", "<" + ruleConfig.getRepurchaseLow() + "%"));
        }
    }

    private void buildReviewSignal(List<SignalDTO> signals, double score) {
        if (score < ruleConfig.getReviewLow()) {
            signals.add(new SignalDTO("REVIEW_LOW", "red", "口碑预警",
                    "平均评分", round2(score) + "", "<" + ruleConfig.getReviewLow()));
        } else if (score >= ruleConfig.getReviewHigh()) {
            signals.add(new SignalDTO("REVIEW_GOOD", "green", "口碑优秀",
                    "平均评分", round2(score) + "", "≥" + ruleConfig.getReviewHigh()));
        }
    }

    private void buildDelaySignal(List<SignalDTO> signals, double d) {
        if (d > ruleConfig.getDelayHigh()) {
            signals.add(new SignalDTO("DELAY_HIGH", "orange", "物流预警",
                    "物流延误率", round1(d) + "%", ">" + ruleConfig.getDelayHigh() + "%"));
        }
    }

    private void buildCancelSignal(List<SignalDTO> signals, double c) {
        if (c > ruleConfig.getCancelHigh()) {
            signals.add(new SignalDTO("CANCEL_HIGH", "red", "退单预警",
                    "退单率", round1(c) + "%", ">" + ruleConfig.getCancelHigh() + "%"));
        }
    }

    // ===================== 结论文案 =====================

    private String buildConclusion(double totalSales, double salesGrowth, String topCategory,
                                    double repurchaseRate, double delayRate, double cancelRate) {
        StringBuilder sb = new StringBuilder();
        sb.append("本期销售额 R$").append(round1(totalSales)).append(" 万，");
        if (salesGrowth > ruleConfig.getSalesGrowthStrong()) {
            sb.append("环比 +").append(round1(salesGrowth)).append("%（增长强劲）");
        } else if (salesGrowth < ruleConfig.getSalesGrowthDecline()) {
            sb.append("环比 ").append(round1(salesGrowth)).append("%（下滑预警）");
        } else {
            sb.append("环比 +").append(round1(salesGrowth)).append("%（平稳）");
        }
        if (topCategory != null && !topCategory.isEmpty()) {
            sb.append("，主要由『").append(topCategory).append("』品类带动");
        }
        sb.append("。");

        List<String> warnings = new ArrayList<>();
        if (repurchaseRate < ruleConfig.getRepurchaseLow()) {
            warnings.add("复购率仅 " + round1(repurchaseRate) + "% 低于健康线 " + ruleConfig.getRepurchaseLow() + "%，建议加强老客召回");
        }
        if (delayRate > ruleConfig.getDelayHigh()) {
            warnings.add("物流延误率 " + round1(delayRate) + "% 偏高，建议优化履约");
        }
        if (cancelRate > ruleConfig.getCancelHigh()) {
            warnings.add("退单率 " + round1(cancelRate) + "% 偏高，建议关注商品与售后");
        }
        if (!warnings.isEmpty()) {
            sb.append("但").append(String.join("；", warnings)).append("。");
        } else {
            sb.append("各健康度指标均在合理区间，经营状态良好。");
        }
        return sb.toString();
    }

    // ===================== 工具 =====================

    private String topCategory(String sp, String ep, String sid) {
        List<Object[]> rows = orderItemRepository.categoryRank(sp, ep, sid);
        if (rows == null || rows.isEmpty()) {
            return "";
        }
        Object[] top = rows.get(0);
        String pt = top[0] == null ? "" : top[0].toString();
        return CategoryConstants.categoryZh(pt);
    }

    private static double toDouble(Object o) {
        return (o instanceof Number) ? ((Number) o).doubleValue() : 0.0;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
