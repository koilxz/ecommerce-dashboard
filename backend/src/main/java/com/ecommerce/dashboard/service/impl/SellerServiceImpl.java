package com.ecommerce.dashboard.service.impl;

import com.ecommerce.dashboard.dto.SellerDTO;
import com.ecommerce.dashboard.repository.SellerRepository;
import com.ecommerce.dashboard.service.SellerService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 卖家（店铺）预设列表实现：聚合 t_order_item 取销量前 5 卖家，城市+州拼装店名。
 */
@Service
public class SellerServiceImpl implements SellerService {

    private final SellerRepository sellerRepository;

    public SellerServiceImpl(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    @Override
    public List<SellerDTO> topSellers() {
        List<Object[]> rows = sellerRepository.topSellers();
        List<SellerDTO> list = new ArrayList<>();
        for (Object[] r : rows) {
            String sellerId = r[0] == null ? "" : r[0].toString();
            String city = r[1] == null ? "" : r[1].toString();
            String state = r[2] == null ? "" : r[2].toString();
            Long orderCount = r[3] == null ? 0L : ((Number) r[3]).longValue();
            String shopName = (city.isEmpty() && state.isEmpty())
                    ? (sellerId + " 店")
                    : (city + "-" + state + "店");
            list.add(new SellerDTO(sellerId, shopName, city, state, orderCount));
        }
        return list;
    }
}
