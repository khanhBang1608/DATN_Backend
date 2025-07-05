package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.PromotionsEntity;

public interface JpaPromotions extends JpaRepository<PromotionsEntity, Integer> {

}
