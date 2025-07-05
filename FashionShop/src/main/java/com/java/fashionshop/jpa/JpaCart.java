package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CartEntity;

public interface JpaCart extends JpaRepository<CartEntity, Integer> {

}
