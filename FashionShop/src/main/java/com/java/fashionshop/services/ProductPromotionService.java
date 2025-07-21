package com.java.fashionshop.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.ProductPromotionBean;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductPromotionEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.jpa.JpaProduct;
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
    
    @Autowired
    private JpaProduct jpaProduct;

    

    public List<ProductVariantDTO> getVariantsByProductId(Integer productId) {
        List<ProductVariantEntity> variants = JpaProductVariant.findByProduct_ProductId(productId);

        return variants.stream().map(variant -> {
            ProductVariantDTO dto = new ProductVariantDTO();
            dto.setProductVariantId(variant.getProductVariantId());
            dto.setStock(variant.getStock());
            dto.setPrice(variant.getPrice());
            dto.setImageName(variant.getImageName());

            if (variant.getColor() != null) {
                dto.setColorId(variant.getColor().getColorId());
                dto.setColorName(variant.getColor().getColorName());
            }

            if (variant.getSize() != null) {
                dto.setSizeId(variant.getSize().getSizeId());
                dto.setSizeName(variant.getSize().getSizeName());
            }

            return dto;
        }).collect(Collectors.toList());
    }
    
    public List<ProductPromotionDTO> findByPromotionId(Integer promotionId) {
        List<ProductPromotionEntity> entities = productPromotionRepo.findByPromotion_Id(promotionId);
        return entities.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ProductPromotionEntity findById(Integer id) {
        return productPromotionRepo.findById(id).orElse(null);
    }



    public List<ProductPromotionDTO> findAll() {
        return productPromotionRepo.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ProductPromotionDTO> saveBulk(Integer promotionId, List<ProductPromotionBean> beans) {
        List<ProductPromotionDTO> result = new ArrayList<>();

        for (ProductPromotionBean bean : beans) {
            PromotionsEntity promotion = JpaPromotion.findById(bean.getPromotionId()).orElse(null);
            ProductVariantEntity variant = JpaProductVariant.findById(bean.getProductVariantId()).orElse(null);

            if (promotion == null || variant == null) continue;

            ProductPromotionEntity entity = new ProductPromotionEntity();
            entity.setPromotion(promotion);
            entity.setProductVariant(variant);
            entity.setQuantityLimit(bean.getQuantityLimit());

            result.add(convertToDTO(productPromotionRepo.save(entity)));
        }

        return result;
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
        dto.setPromotionId(entity.getPromotion().getId()); 
        return dto;
    }

}
