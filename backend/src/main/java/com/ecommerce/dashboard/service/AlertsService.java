package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.AlertDTO;

import java.util.List;

/**
 * 异常预警服务：品类销售额骤降 / 评分骤降 / 支付方式占比突变。
 */
public interface AlertsService {

    /**
     * 计算异常预警列表。
     * @param startDate 开始日期 yyyy-MM-dd（可选）
     * @param endDate   结束日期 yyyy-MM-dd（可选）
     * @param sellerId  卖家(店铺)ID（可选，null=全平台）
     */
    List<AlertDTO> alerts(String startDate, String endDate, String sellerId);
}
