package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductPromotionDTO {

    private Integer id;

    private Integer quantityLimit;

    private Integer productVariantId;

	private Integer promotionId;

}
