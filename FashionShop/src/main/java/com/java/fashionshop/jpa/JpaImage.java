package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ImageEntity;

public interface JpaImage extends JpaRepository<ImageEntity, Integer> {

}
