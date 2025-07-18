package com.java.fashionshop.jpa;

import com.java.fashionshop.entity.ProductViewsEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface JpaProductViews extends JpaRepository<ProductViewsEntity, Long> {
    
    List<ProductViewsEntity> findTop10ByUserOrderBySearchTimeDesc(UserEntity user);

    @Query("SELECT pv.product FROM ProductViewsEntity pv GROUP BY pv.product ORDER BY COUNT(pv.id) DESC")
    List<ProductEntity> findTopViewedProducts();
}
