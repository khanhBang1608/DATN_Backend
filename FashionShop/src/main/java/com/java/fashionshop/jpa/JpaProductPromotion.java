package com.java.fashionshop.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductPromotionEntity;
import com.java.fashionshop.entity.ProductVariantEntity;


public interface JpaProductPromotion extends JpaRepository<ProductPromotionEntity, Integer> {
	List<ProductPromotionEntity> findByPromotion_Id(Integer id);
	List<ProductPromotionEntity> findByProductVariant_ProductVariantId(Integer productVariantId);

}
