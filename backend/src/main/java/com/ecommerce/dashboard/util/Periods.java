package com.ecommerce.dashboard.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * 周期工具：把「所选周期」解析为当前窗口与上一窗口（等长的相邻窗口），用于环比计算。
 * 默认当前窗口取数据集尾部 6 个月（Olist 尾期约 2018-09），上一窗口为前 6 个月。
 * 返回的时间戳格式 yyyy-MM-dd HH:mm:ss，与 Repository 原生 SQL 的 >= / <= 比较一致。
 */
public final class Periods {

    public static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private static final String DEF_CUR_START = "2018-03-01";
    private static final String DEF_CUR_END = "2018-09-30";

    private Periods() {
    }

    /**
     * @return String[4] = { curStartTs, curEndTs, prevStartTs, prevEndTs }
     */
    public static String[] resolve(String startDate, String endDate) {
        String s = (startDate == null || startDate.isBlank()) ? DEF_CUR_START : startDate.trim();
        String e = (endDate == null || endDate.isBlank()) ? DEF_CUR_END : endDate.trim();
        LocalDate curStart = LocalDate.parse(s, FMT);
        LocalDate curEnd = LocalDate.parse(e, FMT);
        long days = ChronoUnit.DAYS.between(curStart, curEnd) + 1;
        LocalDate prevEnd = curStart.minusDays(1);
        LocalDate prevStart = prevEnd.minusDays(days - 1);
        return new String[]{
                curStart.format(FMT) + " 00:00:00",
                curEnd.format(FMT) + " 23:59:59",
                prevStart.format(FMT) + " 00:00:00",
                prevEnd.format(FMT) + " 23:59:59"
        };
    }
}
