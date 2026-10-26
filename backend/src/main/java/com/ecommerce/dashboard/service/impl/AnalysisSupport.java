package com.ecommerce.dashboard.service.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * 分析查询公共工具（供 Signals/Alerts 服务复用，避免与 JpaOrderServiceImpl 重复）
 */
final class AnalysisSupport {

    private AnalysisSupport() {
    }

    static String startParam(String d) {
        return (d == null || d.isBlank()) ? null : d.trim() + " 00:00:00";
    }

    static String endParam(String d) {
        return (d == null || d.isBlank()) ? null : d.trim() + " 23:59:59";
    }

    static String sid(String sellerId) {
        return (sellerId == null || sellerId.isBlank()) ? null : sellerId.trim();
    }

    static double toDouble(Object o) {
        if (o == null) return 0.0;
        if (o instanceof Number) return ((Number) o).doubleValue();
        return 0.0;
    }

    static long toLong(Object o) {
        if (o == null) return 0L;
        if (o instanceof Number) return ((Number) o).longValue();
        return 0L;
    }

    /** 根据起止日期推算上一等长周期（用于环比），无日期返回 null */
    static String[] prevRange(String startDate, String endDate) {
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

    static double growth(double cur, double prev) {
        if (prev == 0) return 0.0;
        return Math.round((cur - prev) / prev * 1000.0) / 10.0;
    }
}
