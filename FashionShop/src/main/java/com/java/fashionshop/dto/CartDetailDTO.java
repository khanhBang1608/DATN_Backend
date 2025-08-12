package com.java.fashionshop.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class CartDetailDTO {
    private Integer cartDetailId;
    private Integer productVariantId;
    private boolean productStatus;
    private boolean categoryStatus;
    private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String imageUrl;
    private String size;
    private String color;
    private Integer stock;
}