package com.java.fashionshop.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.ProductPromotionBean;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.entity.ProductPromotionEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.jpa.JpaProductPromotion;
import com.java.fashionshop.jpa.JpaPromotions;
import com.java.fashionshop.jpa.JpaProductVariant;

@Service
public class ProductPromotionService {

    @Autowired
    private JpaProductPromotion productPromotionRepo;

    @Autowired
    private JpaPromotions JpaPromotion;

    @Autowired
    private JpaProductVariant JpaProductVariant;

    public List<ProductPromotionDTO> findAll() {
        return productPromotionRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductPromotionDTO save(ProductPromotionBean bean) {
    	PromotionsEntity promotion = JpaPromotion.findById(bean.getPromotionId()).orElse(null);
    	ProductVariantEntity variant = JpaProductVariant.findById(bean.getProductVariantId()).orElse(null);

        if (promotion == null || variant == null) return null;

        ProductPromotionEntity entity = new ProductPromotionEntity();
        entity.setQuantityLimit(bean.getQuantityLimit());
        entity.setPromotion(promotion);
        entity.setProductVariant(variant);

        return convertToDTO(productPromotionRepo.save(entity));
    }
    
    public ProductPromotionDTO update(Integer id, ProductPromotionBean bean) {
        ProductPromotionEntity entity = productPromotionRepo.findById(id).orElse(null);
        if (entity == null) return null;

        PromotionsEntity promotion = JpaPromotion.findById(bean.getPromotionId()).orElse(null);
        ProductVariantEntity variant = JpaProductVariant.findById(bean.getProductVariantId()).orElse(null);

        if (promotion == null || variant == null) return null;

        entity.setQuantityLimit(bean.getQuantityLimit());
        entity.setPromotion(promotion);
        entity.setProductVariant(variant);

        return convertToDTO(productPromotionRepo.save(entity));
    }


    public void delete(Integer id) {
        productPromotionRepo.deleteById(id);
    }

    public ProductPromotionDTO convertToDTO(ProductPromotionEntity entity) {
        ProductPromotionDTO dto = new ProductPromotionDTO();
        dto.setId(entity.getId());
        dto.setQuantityLimit(entity.getQuantityLimit());
        dto.setProductVariantId(entity.getProductVariant().getProductVariantId());
        return dto;
    }
}
