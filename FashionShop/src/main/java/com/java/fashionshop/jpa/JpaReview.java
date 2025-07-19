package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ReviewEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaReview extends JpaRepository<ReviewEntity, Integer> {
    @Query("SELECT r FROM ReviewEntity r " +
            "JOIN r.user u " +
            "JOIN r.orderDetail od " +
            "JOIN od.productVariant pv " +
            "JOIN pv.product p " +
            "WHERE (:ratings IS NULL OR r.rating IN :ratings) " +
            "AND (:startDate IS NULL OR r.reviewDate >= :startDate) " +
            "AND (:endDate IS NULL OR r.reviewDate <= :endDate) " +
            "AND (:userFullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :userFullName, '%')))")
    List<ReviewEntity> findReviewsWithFilters(
            @Param("ratings") List<Integer> ratings,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("userFullName") String userFullName);

    List<ReviewEntity> findByOrderDetail_ProductVariant_Product_ProductId(Integer productId);
    List<ReviewEntity> findByUser_UserId(Integer userId);
}
