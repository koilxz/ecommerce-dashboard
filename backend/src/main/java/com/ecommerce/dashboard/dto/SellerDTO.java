package com.ecommerce.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 卖家（店铺）DTO（/api/sellers 元素）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDTO implements Serializable {
    /** 卖家 ID（店铺代理） */
    private String sellerId;
    /** 店铺名（城市 + 州，如 圣保罗-SP 店） */
    private String shopName;
    /** 城市 */
    private String city;
    /** 州 */
    private String state;
    /** 订单数（用于排序取 top5） */
    private Long orderCount;
}
