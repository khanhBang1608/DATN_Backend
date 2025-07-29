package com.java.fashionshop.jpa;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CategoryEntity;

public interface JpaCategory extends JpaRepository<CategoryEntity, Integer> {
    Optional<CategoryEntity> findByCategoryName(String categoryName);
}

