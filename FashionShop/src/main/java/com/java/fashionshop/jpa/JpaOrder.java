package com.java.fashionshop.jpa;

import java.util.List;

import com.java.fashionshop.entity.UserEntity;
import org.hibernate.query.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.OrderEntity;

public interface JpaOrder extends JpaRepository<OrderEntity, Integer> {
	List<OrderEntity> findByUser(UserEntity user);
	List<OrderEntity> findByUser_UserIdOrderByOrderDateDesc(Integer userId);
}
