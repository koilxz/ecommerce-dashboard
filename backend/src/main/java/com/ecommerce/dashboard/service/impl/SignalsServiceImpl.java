package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.config.CategoryConstants;
import com.ecommerce.dashboard.dto.CategorySignalDTO;
import com.ecommerce.dashboard.repository.OrderItemRepository;
import com.ecommerce.dashboard.service.SignalsService;
import com.ecommerce.dashboard.util.Periods;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 补货/促销代理信号实现：
 *  - 当前窗口品类销售额、上一窗口销售额 → 增速；
 *  - 当前窗口品类平均评分；
 *  - 品类销售额占比；
 *  - 综合「增速 × 口碑 × 占比」给出行动建议（重点投入 / 观察 / 促销去化）；
 *  - note 固定为「基于销售趋势推测，非实时库存」。
 */
@Service
public class SignalsServiceImpl implements SignalsService {

    private static final String NOTE = "基于销售趋势推测，非实时库存";

    private final OrderItemRepository orderItemRepository;

    public SignalsServiceImpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public List<CategorySignalDTO> signals(String startDate, String endDate, String sellerId) {
        String[] p = Periods.resolve(startDate, endDate);
        String curStart = p[0], curEnd = p[1], prevStart = p[2], prevEnd = p[3];
        String sid = (sellerId == null || sellerId.isBlank()) ? null : sellerId.trim();

        // 当前窗口：品类销售额、品类平均评分
        Map<String, Double> curSales = rankToMap(orderItemRepository.categoryRank(curStart, curEnd, sid));
        Map<String, Double> curScore = scoreToMap(orderItemRepository.categoryScoreStat(curStart, curEnd, sid));
        // 上一窗口：品类销售额、品类平均评分
        Map<String, Double> prevSales = rankToMap(orderItemRepository.categoryRank(prevStart, prevEnd, sid));
        Map<String, Double> prevScore = scoreToMap(orderItemRepository.categoryScoreStat(prevStart, prevEnd, sid));

        double totalCur = curSales.values().stream().mapToDouble(Double::doubleValue).sum();

        List<CategorySignalDTO> result = new ArrayList<>();
        for (Map.Entry<String, Double> e : curSales.entrySet()) {
            String en = e.getKey();
            double cs = e.getValue();
            double ps = prevSales.getOrDefault(en, 0.0);
            double score = curScore.getOrDefault(en, 0.0);

            double growth = (ps > 0) ? (cs - ps) / ps * 100.0 : (cs > 0 ? 100.0 : 0.0);
            double share = (totalCur > 0) ? cs / totalCur * 100.0 : 0.0;
            String action = decideAction(growth, score, share);

            result.add(new CategorySignalDTO(
                    CategoryConstants.categoryZh(en),
                    en,
                    round1(growth),
                    round2(score),
                    round1(share),
                    action,
                    NOTE));
        }

        // 按销售额降序，便于前端展示头部品类
        result.sort((a, b) -> Double.compare(b.getShare(), a.getShare()));
        return result;
    }

    /** 综合判定行动建议 */
    private String decideAction(double growth, double score, double share) {
        if (growth >= 10 && score >= 4.0) {
            return "重点投入";
        }
        if (growth <= -10 || score < 3.5) {
            return "促销去化";
        }
        return "观察";
    }

    private static Map<String, Double> rankToMap(List<Object[]> rows) {
        Map<String, Double> m = new HashMap<>();
        if (rows == null) return m;
        for (Object[] r : rows) {
            if (r[0] == null) continue;
            String en = r[0].toString();
            double sales = (r[1] instanceof Number) ? ((Number) r[1]).doubleValue() : 0.0;
            m.put(en, sales);
        }
        return m;
    }

    private static Map<String, Double> scoreToMap(List<Object[]> rows) {
        Map<String, Double> m = new HashMap<>();
        if (rows == null) return m;
        for (Object[] r : rows) {
            if (r[0] == null) continue;
            String en = r[0].toString();
            double score = (r[1] instanceof Number) ? ((Number) r[1]).doubleValue() : 0.0;
            m.put(en, score);
        }
        return m;
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static double round2(double v) {
        return Math.round(v * 100.0) / 100.0;
    }
}
