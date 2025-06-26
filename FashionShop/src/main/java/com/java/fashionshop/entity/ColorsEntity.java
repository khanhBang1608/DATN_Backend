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
@Table(name = "colors")
public class ColorsEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer colorId;
    @Column(name = "color_name", nullable = false, unique = true, columnDefinition = "NVARCHAR(20)")
    private String colorName;
}
