package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 异常预警 DTO（/api/alerts 元素）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlertDTO implements Serializable {
    /** 预警类型：CATEGORY_DROP / SCORE_DROP / PAYMENT_SHIFT */
    private String type;
    /** 严重度：red / orange */
    private String severity;
    /** 预警标题 */
    private String title;
    /** 明细说明 */
    private String detail;
    /** 关联品类（无则空） */
    private String relatedCategory;
}
