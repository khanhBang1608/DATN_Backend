package com.java.fashionshop.dto;

import lombok.Data;

@Data
public class FavoriteDTO {
    private Integer id;
    private ProductDTO product;

    public FavoriteDTO() {
    }

    public FavoriteDTO(Integer id, ProductDTO product) {
        this.id = id;
        this.product = product;
    }
}
