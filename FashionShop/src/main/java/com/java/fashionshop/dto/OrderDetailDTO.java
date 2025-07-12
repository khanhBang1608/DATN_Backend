package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class OrderDetailDTO {
    private Integer orderDetailId;
    private Integer quantity;
    private BigDecimal price;
    private Integer productVariantId;
    private String productName;
    private String imageUrl;
    private String size;
    private String color;
}