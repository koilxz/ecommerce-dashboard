package com.ecommerce.dashboard.repository;

import com.ecommerce.dashboard.entity.CategoryTranslation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 品类中英映射 Repository
 */
public interface CategoryTranslationRepository extends JpaRepository<CategoryTranslation, String> {
}
