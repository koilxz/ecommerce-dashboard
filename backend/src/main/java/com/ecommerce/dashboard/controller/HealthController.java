package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.config.DataReadyState;
import com.ecommerce.dashboard.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 就绪检查（借鉴队友版 HealthController）。
 *
 * <p>因本项目配置了 {@code server.servlet.context-path=/api}，故此处映射 {@code /ready}，
 * 实际对外路径为 {@code /api/ready}。
 * <p>公开接口：LoginInterceptor 仅拦截 /diagnosis|/signals|/alerts，本接口不校验 token。
 * <ul>
 *   <li>数据就绪 → HTTP 200 + ApiResponse.success；</li>
 *   <li>数据仍在导入 → HTTP 503 + ApiResponse(503)。</li>
 * </ul>
 */
@RestController
@RequestMapping("/ready")
@Tag(name = "就绪检查", description = "数据是否导入/校验完成（公开，不校验 token）")
public class HealthController {

    private final DataReadyState dataReadyState;

    public HealthController(DataReadyState dataReadyState) {
        this.dataReadyState = dataReadyState;
    }

    @GetMapping
    @Operation(summary = "就绪检查", description = "ready=true 返回 200；数据仍在导入返回 503")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ready() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("ready", dataReadyState.isReady());
        data.put("message", dataReadyState.getMessage());
        if (dataReadyState.isReady()) {
            return ResponseEntity.ok(ApiResponse.success(data));
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiResponse<>(503, dataReadyState.getMessage(), data));
    }
}
