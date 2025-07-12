package com.java.fashionshop.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ProductPromotionEntity;

public interface JpaProductPromotion extends JpaRepository<ProductPromotionEntity, Integer> {
	
}
