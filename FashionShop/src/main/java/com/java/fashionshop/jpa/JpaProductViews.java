package com.java.fashionshop.jpa;

import com.java.fashionshop.entity.ProductViewsEntity;
import com.java.fashionshop.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JpaProductViews extends JpaRepository<ProductViewsEntity, Long> {
    
    List<ProductViewsEntity> findTop10ByUserOrderBySearchTimeDesc(UserEntity user); 
    Page<ProductViewsEntity> findByUserOrderBySearchTimeDesc(UserEntity user, Pageable pageable);
}
