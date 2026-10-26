package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.dto.CategorySignalDTO;
import com.ecommerce.dashboard.service.SignalsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/signals")
@Tag(name = "补货/促销代理信号", description = "按品类给出行动建议（需 X-Token）")
public class SignalsController {

    private final SignalsService signalsService;

    public SignalsController(SignalsService signalsService) {
        this.signalsService = signalsService;
    }

    @GetMapping
    @Operation(summary = "补货/促销代理信号", description = "返回各品类的增速/口碑/占比与行动建议（推测口径）")
    public ApiResponse<List<CategorySignalDTO>> signals(
            @Parameter(description = "开始日期 yyyy-MM-dd（可选）") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd（可选）") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(signalsService.signals(startDate, endDate, blankToNull(sellerId)));
    }

    private String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
