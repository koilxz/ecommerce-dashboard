package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 商品 Repository（提供现有品类列表，供前端下拉框零改动使用）
 */
public interface ProductRepository extends JpaRepository<Product, String> {

    /** 数据集中实际存在的品类（英文），用于拼接中文下拉列表 */
    @Query(value = "SELECT DISTINCT category_name FROM t_product " +
            "WHERE category_name IS NOT NULL AND category_name <> '' ORDER BY category_name", nativeQuery = true)
    List<String> findExistingCategories();
}
