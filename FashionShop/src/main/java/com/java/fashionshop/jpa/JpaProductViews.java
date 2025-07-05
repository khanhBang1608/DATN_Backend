package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ProductViewsEntity;

public interface JpaProductViews extends JpaRepository<ProductViewsEntity, Integer> {

}
