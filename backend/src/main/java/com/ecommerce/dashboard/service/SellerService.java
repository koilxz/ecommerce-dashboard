package com.ecommerce.dashboard.service;

import com.ecommerce.dashboard.dto.SellerDTO;

import java.util.List;

/**
 * 卖家（店铺）预设列表服务：按销量取前 5 名卖家作为演示店铺（Olist 无店铺字段，seller_id 作代理）。
 */
public interface SellerService {

    /**
     * 返回 top5 卖家（城市+州作店名），公开接口，无需 X-Token。
     */
    List<SellerDTO> topSellers();
}
