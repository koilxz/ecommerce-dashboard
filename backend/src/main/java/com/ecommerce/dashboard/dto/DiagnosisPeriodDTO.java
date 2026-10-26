package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 诊断周期 DTO（所选周期起止与文案）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosisPeriodDTO implements Serializable {
    /** 周期起 yyyy-MM-dd */
    private String start;
    /** 周期止 yyyy-MM-dd */
    private String end;
    /** 周期文案：所选周期 */
    private String label;
}
