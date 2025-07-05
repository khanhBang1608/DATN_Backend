package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.DiscountEntity;

public interface JpaDiscount extends JpaRepository<DiscountEntity, Integer> {

}
