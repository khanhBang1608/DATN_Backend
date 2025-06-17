package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.SizesEntity;

public interface JpaSizes extends JpaRepository<SizesEntity, Integer> {

}
