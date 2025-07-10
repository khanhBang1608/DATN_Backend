package com.java.fashionshop.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantBean {
    private Integer variantId;
    private Integer productId;
    private Integer colorId;
    private String colorName;
    private Integer sizeId;
    private String sizeName;
    private BigDecimal price;
    private Integer stock;
    private MultipartFile image;
}

