package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ProductPromotionEntity;

public interface JpaProductPromotion extends JpaRepository<ProductPromotionEntity, Integer> {

}
