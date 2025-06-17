package com.java.fashionshop.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "sizes")
public class SizesEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer sizeId;
    @Column(nullable = false, unique = true, length = 10)
    private String sizeName;
}
