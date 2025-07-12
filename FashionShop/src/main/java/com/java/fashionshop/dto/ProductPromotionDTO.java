package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionDTO {

    private Integer id;

    private Integer quantityLimit;

    private Integer productVariantId;

    private String productVariantName;

    private String color;

    private String size;
}
