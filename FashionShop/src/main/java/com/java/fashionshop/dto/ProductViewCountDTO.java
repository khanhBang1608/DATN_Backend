package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductViewCountDTO {
    private ProductDTO product;
    private Long viewCount;
}

