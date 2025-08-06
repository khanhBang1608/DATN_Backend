package com.java.fashionshop.jpa;

import com.java.fashionshop.entity.OrderEntity;
import com.java.fashionshop.entity.OrderReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaOrderReturnEntity extends JpaRepository<OrderReturnEntity, Integer> {
    Optional<OrderReturnEntity> findByOrder(OrderEntity order);
}

