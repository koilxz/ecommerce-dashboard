package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.DiagnosisDTO;

/**
 * 经营健康度诊断服务接口（规则引擎）
 */
public interface DiagnosisService {

    /**
     * 诊断所选周期的经营健康度
     * @param startDate 开始日期 yyyy-MM-dd（可选）
     * @param endDate   结束日期 yyyy-MM-dd（可选）
     * @param sellerId  卖家(店铺)ID（可选，null=全平台）
     */
    DiagnosisDTO diagnose(String startDate, String endDate, String sellerId);
}
