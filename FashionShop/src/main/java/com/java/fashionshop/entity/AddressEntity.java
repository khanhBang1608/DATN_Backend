package com.java.fashionshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor
@Table(name = "address")
public class AddressEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String customerName;
    private String phone;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(300)")
    private String address;
    private Integer provinceId;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String provinceName;
    private Integer districtId;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String districtName;
    private Integer wardId;
    
    @Column(nullable = false, columnDefinition = "NVARCHAR(100)")
    private String wardName;
}

