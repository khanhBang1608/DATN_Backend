package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.CartDetailEntity;

public interface JpaCartDetail extends JpaRepository<CartDetailEntity, Integer> {

}
