package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.dto.DiagnosisDTO;
import com.ecommerce.dashboard.service.DiagnosisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/diagnosis")
@Tag(name = "经营健康度诊断", description = "规则引擎：KPI 信号 + 人话结论（需 X-Token）")
public class DiagnosisController {

    private final DiagnosisService diagnosisService;

    public DiagnosisController(DiagnosisService diagnosisService) {
        this.diagnosisService = diagnosisService;
    }

    @GetMapping
    @Operation(summary = "经营健康度诊断", description = "返回信号标签数组 + 服务端拼装的人话结论")
    public ApiResponse<DiagnosisDTO> diagnose(
            @Parameter(description = "开始日期 yyyy-MM-dd") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期 yyyy-MM-dd") @RequestParam(required = false) String endDate,
            @Parameter(description = "卖家(店铺)ID，可选") @RequestParam(required = false, defaultValue = "") String sellerId) {
        return ApiResponse.success(diagnosisService.diagnose(startDate, endDate, blankToNull(sellerId)));
    }

    private String blankToNull(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v.trim();
    }
}
