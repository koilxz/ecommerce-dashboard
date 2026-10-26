package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 诊断信号标签 DTO（diagnosis.signals 元素）
 * level ∈ green / gray / red / orange，前端按语义着色。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignalDTO implements Serializable {
    private String code;       // 信号编码
    private String level;      // 语义色 green/gray/red/orange
    private String label;      // 信号名
    private String metric;     // 指标名
    private String value;      // 实际值
    private String threshold;  // 触发阈值
}
