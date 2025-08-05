package com.java.fashionshop.controller;

import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.FavoriteDTO;
import com.java.fashionshop.entity.FavoriteEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaFavorite;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.services.FavoriteService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;
    
    @Autowired
    private JpaFavorite jpaFavorite;

    @Autowired
    private JpaUser jpaUser;

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/{productId}")
    public ResponseEntity<?> toggleFavorite(HttpServletRequest request, @PathVariable Integer productId) {
        try {
            String result = favoriteService.toggleFavorite(request, productId);
            return ResponseEntity.ok(result);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
    
    @GetMapping("")
    public ResponseEntity<?> getFavorites(HttpServletRequest request) {
        try {
            List<FavoriteDTO> favorites = favoriteService.getFavorites(request);
            return ResponseEntity.ok(favorites);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi máy chủ: " + e.getMessage());
        }
    }
    
    @GetMapping("/check/{productId}")
    public ResponseEntity<Boolean> checkFavorite(HttpServletRequest request, @PathVariable Integer productId) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(false);
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            Optional<UserEntity> userOpt = jpaUser.findByEmail(email);
            Optional<ProductEntity> productOpt = jpaProduct.findById(productId);

            if (userOpt.isEmpty() || productOpt.isEmpty()) {
                return ResponseEntity.ok(false); // hoặc 404 tùy bạn muốn
            }

            boolean exists = jpaFavorite.findByUserAndProduct(userOpt.get(), productOpt.get()).isPresent();
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(false);
        }
    }
    
}
