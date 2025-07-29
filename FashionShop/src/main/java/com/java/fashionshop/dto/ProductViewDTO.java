package com.java.fashionshop.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
@Data
public class ProductViewDTO {
    private Integer id;

    private List<ProductDTO> product;

    private LocalDateTime searchTime;

    private List<UserDTO> user;
}
