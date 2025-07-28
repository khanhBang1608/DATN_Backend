package com.java.fashionshop.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantDTO {
    private Integer productVariantId;
    private Integer stock;
    private BigDecimal price;
    private String imageName;
    private Integer colorId;
    private String colorName;
    private Integer sizeId;
    private String sizeName;
}
