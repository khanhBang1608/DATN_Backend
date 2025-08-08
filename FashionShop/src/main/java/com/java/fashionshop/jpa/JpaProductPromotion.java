package com.java.fashionshop.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.ProductPromotionEntity;

public interface JpaProductPromotion extends JpaRepository<ProductPromotionEntity, Integer> {
	List<ProductPromotionEntity> findByPromotion_Id(Integer id);
	List<ProductPromotionEntity> findByProductVariant_ProductVariantId(Integer productVariantId);
	Page<ProductPromotionEntity> findByPromotion_Id(Integer promotionId, Pageable pageable);
}
