package com.java.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "discount")
@Data
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "discount_id")
    private Integer discountId;

    @Column(name = "discount_code", nullable = false, unique = true)
    private String discountCode;

    @Column(name = "discount_percent", nullable = false)
    private Double discountPercent;

    @Column(name = "min_order_amount")
    private Double minOrderAmount;

    @Column(name = "max_discount_amount") 
    private Double maxDiscountAmount;

    @Column(name = "quantity_limit")
    private Integer quantityLimit;

    @Column(name = "start_date")
    private LocalDate startDate; 

    @Column(name = "end_date")
    private LocalDate endDate;   

    private Boolean status;

}
