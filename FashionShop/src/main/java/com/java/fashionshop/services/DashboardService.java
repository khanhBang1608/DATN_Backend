package com.java.fashionshop.services;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.jpa.JpaUser;

@Service
public class DashboardService {

    @Autowired
    private JpaOrder jpaOrder;

    @Autowired
    private JpaUser jpaUser;

    // ✅ Doanh thu
    public List<Map<String, Object>> getMonthlyRevenueFullYear() {
        List<Map<String, Object>> rawData = jpaOrder.getMonthlyRevenueFullYear();

        Map<Integer, BigDecimal> revenueMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row.get("month")).intValue(),
                        row -> (BigDecimal) row.get("revenue")
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("month", i);
            row.put("revenue", revenueMap.getOrDefault(i, BigDecimal.ZERO));
            result.add(row);
        }
        return result;
    }

    // ✅ Người dùng đăng ký
    public List<Map<String, Object>> getMonthlyUserRegistrationsFullYear() {
        List<Map<String, Object>> rawData = jpaUser.countUsersByMonthYear();

        Map<String, Integer> userMap = rawData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row.get("monthYear"),
                        row -> ((Number) row.get("userCount")).intValue()
                ));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            String key = String.format("%02d/%d", i, LocalDateTime.now().getYear());
            Map<String, Object> row = new HashMap<>();
            row.put("monthYear", key);
            row.put("userCount", userMap.getOrDefault(key, 0));
            result.add(row);
        }
        return result;
    }
}

