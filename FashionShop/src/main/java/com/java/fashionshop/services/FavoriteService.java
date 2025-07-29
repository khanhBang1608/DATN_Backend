package com.java.fashionshop.services;

import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.FavoriteDTO;
import com.java.fashionshop.entity.FavoriteEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaFavorite;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

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

    /**
     * Xử lý logic: Toggle yêu thích theo productId và request token.
     * @return String message kết quả
     */
    public String toggleFavorite(HttpServletRequest request, Integer productId) {
        // ✅ Lấy token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Thiếu token.");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        // ✅ Lấy user & product
        Optional<UserEntity> optionalUser = jpaUser.findByEmail(email);
        Optional<ProductEntity> optionalProduct = jpaProduct.findById(productId);

        if (optionalUser.isEmpty() || optionalProduct.isEmpty()) {
            throw new RuntimeException("User hoặc Product không tồn tại.");
        }

        UserEntity user = optionalUser.get();
        ProductEntity product = optionalProduct.get();

        // ✅ Kiểm tra đã yêu thích chưa
        Optional<FavoriteEntity> existing = jpaFavorite.findByUserAndProduct(user, product);

        if (existing.isPresent()) {
            // Đã có thì xóa
            jpaFavorite.delete(existing.get());
            return "Đã gỡ khỏi yêu thích.";
        } else {
            // Chưa có thì thêm
            FavoriteEntity favorite = new FavoriteEntity();
            favorite.setUser(user);
            favorite.setProduct(product);
            jpaFavorite.save(favorite);
            return "Đã thêm vào yêu thích.";
        }
    }
    
 // Trong FavoriteService.java

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
                .map(fav -> {
                    ProductEntity product = fav.getProduct();
                    ProductVariantEntity variant = product.getVariants().stream().findFirst().orElse(null);

                    return new FavoriteDTO(
                        fav.getFavoriteId(),
                        product.getProductId(), 
                        product.getName(),
                        variant != null ? variant.getImageName() : null
                    );
                })
                .toList();
    }

}
