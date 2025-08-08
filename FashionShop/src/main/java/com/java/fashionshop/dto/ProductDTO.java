package com.java.fashionshop.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductDTO {
    private Integer productId;
    private String name;
    private String description;
    private LocalDateTime dateCreated;
    private Boolean status;
    private Integer categoryId;
    private String categoryName;
    private Boolean categoryStatus;
    private Integer viewCount;
    private Integer favoriteCount;
    private Integer cartCount;
    private List<ProductVariantDTO> variants;
    
}
