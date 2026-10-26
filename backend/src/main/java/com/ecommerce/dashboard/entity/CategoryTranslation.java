package com.ecommerce.dashboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 品类中英映射实体（对应 t_category_translation）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_category_translation")
public class CategoryTranslation implements Serializable {

    /** 英文品类名（主键，与 Olist 一致） */
    @Id
    @Column(name = "category_name", length = 64)
    private String categoryName;

    @Column(name = "category_name_english", length = 64)
    private String categoryNameEnglish;
}
