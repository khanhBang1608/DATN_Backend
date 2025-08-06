package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.DiscountEntity;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JpaDiscount extends JpaRepository<DiscountEntity, Integer> {
    Optional<DiscountEntity> findByDiscountCode(String code);

    Page<DiscountEntity> findAll(Pageable pageable);
}
