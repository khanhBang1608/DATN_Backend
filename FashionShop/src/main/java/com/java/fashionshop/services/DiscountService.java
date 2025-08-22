package com.java.fashionshop.services;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.jpa.JpaDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.java.fashionshop.dto.DiscountDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class DiscountService {

    @Autowired
    private JpaDiscount jpaDiscount;

    public List<DiscountEntity> findAll() {
        return jpaDiscount.findAll();
    }
    
    public Page<DiscountDTO> getDiscountDTOs(Pageable pageable) {
        return jpaDiscount.findAll(pageable)
                .map(this::convertToDTO);
    }

    public DiscountEntity findById(Integer id) {
        return jpaDiscount.findById(id).orElse(null);
    }

    public DiscountEntity save(DiscountBean bean) {
        DiscountEntity entity = new DiscountEntity();
        copyBeanToEntity(bean, entity);
        return jpaDiscount.save(entity);
    }

    public DiscountEntity update(Integer id, DiscountBean bean) {
        Optional<DiscountEntity> optional = jpaDiscount.findById(id);
        if (optional.isPresent()) {
            DiscountEntity entity = optional.get();
            copyBeanToEntity(bean, entity);
            return jpaDiscount.save(entity);
        }
        return null;
    }

    public void delete(Integer id) {
        jpaDiscount.deleteById(id);
    }

    private void copyBeanToEntity(DiscountBean bean, DiscountEntity entity) {
        entity.setDiscountCode(bean.getDiscountCode());
        entity.setDiscountPercent(bean.getDiscountPercent());
        entity.setMinOrderAmount(bean.getMinOrderAmount());
        entity.setMaxDiscountAmount(bean.getMaxDiscountAmount());
        entity.setQuantityLimit(bean.getQuantityLimit());
        entity.setStartDate(bean.getStartDate());
        entity.setEndDate(bean.getEndDate());
        entity.setStatus(bean.getStatus());
    }
    public List<DiscountEntity> getAvailableDiscounts() {
        LocalDateTime now = LocalDateTime.now(); 
        return jpaDiscount.findAll().stream()
            .filter(d -> Boolean.TRUE.equals(d.getStatus()))
            .filter(d -> (d.getStartDate() == null || !d.getStartDate().isAfter(now)) &&
                         (d.getEndDate() == null || !d.getEndDate().isBefore(now)))
            .collect(Collectors.toList());
    }

    public DiscountDTO convertToDTO(DiscountEntity entity) {
        if (entity == null) return null;

        DiscountDTO dto = new DiscountDTO();
        dto.setDiscountId(entity.getDiscountId());
        dto.setDiscountCode(entity.getDiscountCode());
        dto.setDiscountPercent(entity.getDiscountPercent());
        dto.setMinOrderAmount(entity.getMinOrderAmount());
        dto.setMaxDiscountAmount(entity.getMaxDiscountAmount());
        dto.setQuantityLimit(entity.getQuantityLimit());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setStatus(entity.getStatus());
        
        
        dto.setOrders(null); 
        return dto;
    }

    public List<DiscountDTO> getAllDTOs() {
        return jpaDiscount.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<DiscountDTO> getAvailableDiscountDTOs() {
        LocalDateTime now = LocalDateTime.now(); 
        return jpaDiscount.findAll().stream()
                .filter(d -> Boolean.TRUE.equals(d.getStatus()))
                .filter(d -> (d.getStartDate() == null || !d.getStartDate().isAfter(now)) &&
                             (d.getEndDate() == null || !d.getEndDate().isBefore(now)))
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


}
