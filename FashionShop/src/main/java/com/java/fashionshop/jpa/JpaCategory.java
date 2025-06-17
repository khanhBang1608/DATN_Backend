package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CategoryEntity;

public interface JpaCategory extends JpaRepository<CategoryEntity, Integer> {

}
