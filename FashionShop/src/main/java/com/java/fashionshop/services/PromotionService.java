package com.java.fashionshop.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.PromotionBean;
import com.java.fashionshop.dto.PromotionDTO;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.jpa.JpaPromotions;

@Service
public class PromotionService {

    @Autowired
    private JpaPromotions promotionRepo;

    public List<PromotionDTO> findAll() {
        return promotionRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public PromotionDTO findById(Integer id) {
        return promotionRepo.findById(id).map(this::convertToDTO).orElse(null);
    }

    public PromotionDTO save(PromotionBean bean) {
        PromotionsEntity entity = new PromotionsEntity();
        entity.setCode(bean.getCode());
        entity.setDescription(bean.getDescription());
        entity.setDiscountAmount(bean.getDiscountAmount());
        entity.setStartDate(bean.getStartDate());
        entity.setEndDate(bean.getEndDate());
        entity.setStatus(bean.getStatus());
        return convertToDTO(promotionRepo.save(entity));
    }
    
    public PromotionDTO update(Integer id, PromotionBean bean) {
        PromotionsEntity entity = promotionRepo.findById(id).orElse(null);
        if (entity == null) return null;

        entity.setCode(bean.getCode());
        entity.setDescription(bean.getDescription());
        entity.setDiscountAmount(bean.getDiscountAmount());
        entity.setStartDate(bean.getStartDate());
        entity.setEndDate(bean.getEndDate());
        entity.setStatus(bean.getStatus());

        return convertToDTO(promotionRepo.save(entity));
    }


    public void delete(Integer id) {
        promotionRepo.deleteById(id);
    }

    private PromotionDTO convertToDTO(PromotionsEntity entity) {
        PromotionDTO dto = new PromotionDTO();
        dto.setId(entity.getId());
        dto.setCode(entity.getCode());
        dto.setDescription(entity.getDescription());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        return dto;
    }
}

