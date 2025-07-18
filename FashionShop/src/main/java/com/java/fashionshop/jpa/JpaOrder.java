package com.java.fashionshop.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.OrderEntity;

public interface JpaOrder extends JpaRepository<OrderEntity, Integer> {

	List<OrderEntity> findByUser_UserIdOrderByOrderDateDesc(Integer userId);
}
