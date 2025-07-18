package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.OrderDetailEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaOrderDetail extends JpaRepository<OrderDetailEntity, Integer> {
    @Query("""
    SELECT od
    FROM OrderDetailEntity od
    JOIN od.order o
    JOIN od.productVariant pv
    JOIN pv.product p
    WHERE p.productId = :productId
      AND o.user.userId = :userId
      AND od.review IS NULL
""")
    Optional<OrderDetailEntity> findUnreviewedOrderDetailByProductIdAndUserId(
            @Param("productId") Integer productId,
            @Param("userId") Integer userId);
}
