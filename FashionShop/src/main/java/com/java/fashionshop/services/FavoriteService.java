package com.java.fashionshop.services;

import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.FavoriteDTO;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.entity.FavoriteEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaFavorite;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaUser;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FavoriteService {

    @Autowired
    private JpaFavorite jpaFavorite;

    @Autowired
    private JpaUser jpaUser;

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private ProductService productService;

    public String toggleFavorite(HttpServletRequest request, Integer productId) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token.");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        Optional<UserEntity> optionalUser = jpaUser.findByEmail(email);
        Optional<ProductEntity> optionalProduct = jpaProduct.findById(productId);

        if (optionalUser.isEmpty() || optionalProduct.isEmpty()) {
            throw new RuntimeException("User hoặc Product không tồn tại.");
        }

        UserEntity user = optionalUser.get();
        ProductEntity product = optionalProduct.get();

        Optional<FavoriteEntity> existing = jpaFavorite.findByUserAndProduct(user, product);

        if (existing.isPresent()) {
            jpaFavorite.delete(existing.get());
            return "Đã gỡ khỏi yêu thích.";
        } else {
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUser(user);
            favorite.setProduct(product);
            jpaFavorite.save(favorite);
            return "Đã thêm vào yêu thích.";
        }
    }

    public List<FavoriteDTO> getFavorites(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token.");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        Optional<UserEntity> userOpt = jpaUser.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Người dùng không tồn tại.");
        }

        List<FavoriteEntity> favoriteEntities = jpaFavorite.findByUser(userOpt.get());

        return favoriteEntities.stream()
                .map(this::convertToFavoriteDTO)
                .collect(Collectors.toList());
    }

    private FavoriteDTO convertToFavoriteDTO(FavoriteEntity fav) {
        ProductDTO productDTO = productService.convertToDTO(fav.getProduct());
        return new FavoriteDTO(fav.getFavoriteId(), productDTO);
    }
}
