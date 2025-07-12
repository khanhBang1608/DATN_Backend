package com.java.fashionshop.services;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.jpa.JpaDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DiscountService {

    @Autowired
    private JpaDiscount jpaDiscount;

    public List<DiscountEntity> findAll() {
        return jpaDiscount.findAll();
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
        entity.setDescription(bean.getDescription());
        entity.setDiscountPercent(bean.getDiscountPercent());
        entity.setMinOrderAmount(bean.getMinOrderAmount());
        entity.setMaxDiscountAmount(bean.getMaxDiscountAmount());
        entity.setQuantityLimit(bean.getQuantityLimit());
        entity.setStartDate(bean.getStartDate());
        entity.setEndDate(bean.getEndDate());
        entity.setStatus(bean.getStatus());
    }
}
