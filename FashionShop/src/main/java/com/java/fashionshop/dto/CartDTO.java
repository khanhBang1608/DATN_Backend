package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class CartDTO {
    private Integer cartId;
    private Integer userId;
    private List<CartDetailDTO> details;
}
