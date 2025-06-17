package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.OrderDetailEntity;

public interface JpaOrderDetail extends JpaRepository<OrderDetailEntity, Integer> {

}
