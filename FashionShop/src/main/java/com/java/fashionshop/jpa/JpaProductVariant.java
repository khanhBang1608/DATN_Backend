package com.java.fashionshop.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ColorsEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.SizesEntity;

public interface JpaProductVariant extends JpaRepository<ProductVariantEntity, Integer> {
	List<ProductVariantEntity> findByProduct_ProductId(Integer productId);
	boolean existsByColor(ColorsEntity color);
    boolean existsBySize(SizesEntity size);
}
