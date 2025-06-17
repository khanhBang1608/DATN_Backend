package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.SubCategoryEntity;

public interface JpaSubCategory extends JpaRepository<SubCategoryEntity, Integer> {

}
