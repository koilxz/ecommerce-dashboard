package com.ecommerce.dashboard.controller;

import com.ecommerce.dashboard.dto.ApiResponse;
import com.ecommerce.dashboard.dto.SellerDTO;
import com.ecommerce.dashboard.service.SellerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sellers")
@Tag(name = "卖家(店铺)预设列表", description = "按销量取前 5 名卖家支撑视角切换（公开）")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @GetMapping
    @Operation(summary = "卖家(店铺)预设列表", description = "返回 top5 演示店铺：城市+州作店名，订单数")
    public ApiResponse<List<SellerDTO>> sellers() {
        return ApiResponse.success(sellerService.topSellers());
    }
}
