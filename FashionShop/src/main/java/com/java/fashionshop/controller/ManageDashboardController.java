package com.java.fashionshop.controller;

import com.java.fashionshop.dto.DashboardStatsDTO;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaReview;
import com.java.fashionshop.jpa.JpaUser;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class ManageDashboardController {
	@Autowired
	private JpaOrder orderRepository;

    private final JpaUser jpaUser;
    private final JpaCategory jpaCategory;
    private final JpaProduct jpaProduct;
    private final JpaOrder jpaOrder;
    private final JpaReview jpaReview;

    @GetMapping("/api/admin/dashboard/stats")
    public DashboardStatsDTO getDashboardStats() {
    	long userCount = jpaUser.countByRole(1);
        long totalCategories = jpaCategory.count();
        long totalProducts = jpaProduct.count();
        long totalOrders = jpaOrder.count();
        long totalReviews = jpaReview.count();
        BigDecimal totalRevenue = jpaOrder.getTotalRevenueFromVNPayOrVNPayPending();

        return new DashboardStatsDTO(
        		userCount,
                totalCategories,
                totalProducts,
                totalOrders,
                totalReviews,
                totalRevenue
        );
    }
    
    @GetMapping("/api/admin/dashboard/stats/monthly-revenue")
    public ResponseEntity<List<Map<String, Object>>> getMonthlyRevenue() {
        return ResponseEntity.ok(orderRepository.getMonthlyRevenue());
    }

}
