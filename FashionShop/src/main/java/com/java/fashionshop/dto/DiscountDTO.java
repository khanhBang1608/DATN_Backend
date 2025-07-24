package com.java.fashionshop.dto;

import java.time.LocalDate;
import java.util.List;

import com.java.fashionshop.entity.OrderEntity;

import lombok.Data;
@Data
public class DiscountDTO {

	private Integer discountId;

	private String discountCode;

	private String description;

	private Double discountPercent;

	private Double minOrderAmount;

	private Double maxDiscountAmount;

	private Integer quantityLimit;

	private LocalDate startDate;

	private LocalDate endDate;

	private Boolean status;

	private List<OrderEntity> orders;

}
