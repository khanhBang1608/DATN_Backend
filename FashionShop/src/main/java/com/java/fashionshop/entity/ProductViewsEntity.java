package com.java.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_views")
@Data
public class ProductViewsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private ProductEntity product;

    @Column(name = "search_time")
    private LocalDateTime searchTime;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

}
