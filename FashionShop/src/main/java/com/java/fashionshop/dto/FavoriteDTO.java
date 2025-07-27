package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
	private Integer id;
    private Integer productId;
    private String productName;
    private String image;
    private Integer price;
    private String description;
}
