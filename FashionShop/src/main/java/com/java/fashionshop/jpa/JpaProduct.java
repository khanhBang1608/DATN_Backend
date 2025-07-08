package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ProductEntity;

public interface JpaProduct extends JpaRepository<ProductEntity, Integer> {
	boolean existsById(Integer id);
}
