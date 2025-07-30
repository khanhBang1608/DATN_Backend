package com.java.fashionshop.jpa;

import com.java.fashionshop.entity.FavoriteEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.UserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface JpaFavorite extends JpaRepository<FavoriteEntity, Integer> {
    Optional<FavoriteEntity> findByUserAndProduct(UserEntity user, ProductEntity product);
    List<FavoriteEntity> findByUser(UserEntity user);
    
    @Query("SELECT COUNT(f) FROM FavoriteEntity f WHERE f.product.productId = :productId")
    Long countByProductId(@Param("productId") Integer productId);
}
