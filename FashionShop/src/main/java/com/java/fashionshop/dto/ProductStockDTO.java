package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductStockDTO {
	private Integer productId;
	private Long totalStock;
}
