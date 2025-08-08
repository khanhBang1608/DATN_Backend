package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.PromotionsEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface JpaPromotions extends JpaRepository<PromotionsEntity, Integer> {
    Page<PromotionsEntity> findAll(Pageable pageable);
}

