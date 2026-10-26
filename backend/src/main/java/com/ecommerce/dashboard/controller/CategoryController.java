package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 需求文档 A1 的路径对齐控制器。
 *
 * <p>课程需求文档项目二明确规定 A1 = {@code GET /api/categories}，
 * 而本项目主控制器挂在 {@code /orders} 下（{@code /api/orders/categories}）。
 * 为通过按文档逐条核对接口的验收，这里提供一条等价别名，两处路径均可访问，
 * 前端仍使用 {@code /api/orders/categories}，不影响既有实现。
 */
@RestController
@Tag(name = "品类接口", description = "需求 A1 的文档路径 /api/categories（与 /api/orders/categories 等价）")
public class CategoryController {

    private final OrderService orderService;

    public CategoryController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/categories")
    @Operation(summary = "A1 · 获取商品品类列表（文档路径）",
            description = "返回所有可筛选的商品品类名称（中文），等价于 /api/orders/categories")
    public ApiResponse<List<String>> getCategories() {
        return ApiResponse.success(orderService.getCategoryList());
    }
}
