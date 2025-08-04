package com.java.fashionshop.controller;

import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.ProductViewsService;
import com.java.fashionshop.services.UserService;
import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.ProductViewDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class ProductViewsController {

    @Autowired
    private ProductViewsService productViewsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    // ✅ Hàm lấy userId từ token
    private Integer extractUserIdFromRequest(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);
            Optional<UserEntity> userOpt = userService.findByEmail(email);
            return userOpt.map(UserEntity::getUserId).orElse(null);
        }
        return null;
    }

    // 1. Ghi nhận lượt xem
    @PostMapping("/product-views/record")
    public void recordView(@RequestParam Integer productId, HttpServletRequest request) {
        Integer userId = extractUserIdFromRequest(request);
        if (userId != null) {
            productViewsService.recordView(productId, userId);
        }
    }

    // 2. Lấy sản phẩm đã xem gần đây
    @GetMapping("/product-views/recent")
    public Page<ProductViewDTO> getRecentViews(
            HttpServletRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Integer userId = extractUserIdFromRequest(request);
        if (userId == null) return Page.empty();

        Pageable pageable = PageRequest.of(page, size);
        return productViewsService.getRecentViewDTOsPaged(userId, pageable);
    }

}
