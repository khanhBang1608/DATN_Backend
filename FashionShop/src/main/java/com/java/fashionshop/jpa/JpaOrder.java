package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.OrderEntity;

public interface JpaOrder extends JpaRepository<OrderEntity, Integer> {

}
