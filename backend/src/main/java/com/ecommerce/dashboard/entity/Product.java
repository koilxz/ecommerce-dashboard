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
 * 商品实体（对应 t_product，Olist 无商品名，仅存属性；中文品类经映射得到）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_product")
public class Product implements Serializable {

    @Id
    @Column(name = "product_id", length = 32)
    private String productId;

    /** 英文品类名（如 bed_bath_table），中文在 Service 层经 t_category_translation 映射 */
    @Column(name = "category_name", length = 64)
    private String categoryName;

    @Column(name = "name_length")
    private Integer nameLength;

    @Column(name = "description_length")
    private Integer descriptionLength;

    @Column(name = "photos_qty")
    private Integer photosQty;

    @Column(name = "weight_g")
    private Integer weightG;

    @Column(name = "length_cm")
    private Integer lengthCm;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "width_cm")
    private Integer widthCm;
}
