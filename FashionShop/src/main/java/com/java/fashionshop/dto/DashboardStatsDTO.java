package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalCategories;
    private long totalProducts;
    private long totalOrders;
    private long totalReviews;
    private BigDecimal totalRevenue;
}
