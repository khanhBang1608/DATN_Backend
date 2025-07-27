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
    private Integer viewCount;
    private List<ProductVariantDTO> variants;
}
