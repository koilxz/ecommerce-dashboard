package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.AlertDTO;
import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.service.AlertsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@Tag(name = "异常预警", description = "品类骤降/口碑下滑/支付突变（需 X-Token）")
public class AlertsController {

    private final AlertsService alertsService;

    public AlertsController(AlertsService alertsService) {
        this.alertsService = alertsService;
    }

    @GetMapping
    @Operation(summary = "异常预警", description = "返回品类销售骤降/口碑下滑/支付方式突变等预警")
    public ApiResponse<List<AlertDTO>> alerts(
            @Parameter(description = "开始日期 yyyy-MM-dd（可选）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd（可选）") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(alertsService.alerts(startDate, endDate, blankToNull(sellerId)));
    }

    private String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
