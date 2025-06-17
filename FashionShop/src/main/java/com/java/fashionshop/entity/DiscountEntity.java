package com.java.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "discount")
@Data
public class DiscountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Double percentage;

    private String description;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean status;

     @OneToMany(mappedBy = "discount")
     private List<OrderEntity> orders;
}
