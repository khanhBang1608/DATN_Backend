package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CartDetailEntity;

import java.util.Optional;

public interface JpaCartDetail extends JpaRepository<CartDetailEntity, Integer> {
    Optional<CartDetailEntity> findByCartCartIdAndProductVariantProductVariantId(Integer cartId, Integer productVariantId);
}
