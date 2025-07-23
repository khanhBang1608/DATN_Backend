package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.DiscountEntity;
import java.util.Optional;

public interface JpaDiscount extends JpaRepository<DiscountEntity, Integer> {
    Optional<DiscountEntity> findByDiscountCode(String code);

//    <T> ScopedValue<T> findByDiscountId(Integer discountId);
}
