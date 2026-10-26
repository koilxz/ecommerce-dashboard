package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 补货/促销代理信号 DTO（/api/signals 元素）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategorySignalDTO implements Serializable {
    /** 品类中文名 */
    private String category;
    /** 品类英文名（葡萄牙语存储值） */
    private String categoryEn;
    /** 销售增速 %（本期 vs 上期） */
    private Double growthRate;
    /** 口碑评分（0-5） */
    private Double avgScore;
    /** 该品类销售额占比 % */
    private Double share;
    /** 重点投入 / 观察 / 促销去化 */
    private String action;
    /** 固定文案：基于销售趋势推测，非实时库存 */
    private String note;
}
