package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.CategorySignalDTO;

import java.util.List;

/**
 * 补货/促销代理信号服务：按品类给出「销售增速 × 口碑 × 占比」分位映射的行动建议。
 * 注：这是基于销售趋势的【推测口径】，非实时库存，前端须标注。
 */
public interface SignalsService {

    /**
     * 计算各品类的代理信号。
     * @param startDate 开始日期 yyyy-MM-dd（可选，默认尾部 6 个月）
     * @param endDate   结束日期 yyyy-MM-dd（可选）
     * @param sellerId  卖家(店铺)ID（可选，null=全平台）
     */
    List<CategorySignalDTO> signals(String startDate, String endDate, String sellerId);
}
