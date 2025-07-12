package com.java.fashionshop.bean;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DiscountBean {
    private Integer discountId;
    private String discountCode;
    private String description;
    private Double discountPercent;
    private Double minOrderAmount;
    private Double maxDiscountAmount;
    private Integer quantityLimit;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean status;
}
