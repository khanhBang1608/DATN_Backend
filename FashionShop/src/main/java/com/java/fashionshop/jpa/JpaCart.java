package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CartEntity;

import java.util.Optional;

public interface JpaCart extends JpaRepository<CartEntity, Integer> {
    Optional<CartEntity> findByUserUserId(Integer userId);
}
