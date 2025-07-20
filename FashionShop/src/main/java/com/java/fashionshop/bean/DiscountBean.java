package com.java.fashionshop.bean;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DiscountBean {
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
}
