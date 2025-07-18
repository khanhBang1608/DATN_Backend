package com.java.fashionshop.services;

import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductViewsEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaProductViews;
import com.java.fashionshop.jpa.JpaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductViewsService {

    @Autowired
    private JpaProductViews jpaProductViews;

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private JpaUser jpaUser;

    public void recordView(Integer productId, Integer userId) {
        ProductViewsEntity view = new ProductViewsEntity();
        view.setProduct(jpaProduct.findById(productId).orElse(null));
        view.setUser(jpaUser.findById(userId).orElse(null));
        view.setSearchTime(LocalDateTime.now());
        jpaProductViews.save(view);
    }

    public List<ProductViewsEntity> getRecentViews(Integer userId) {
        UserEntity user = jpaUser.findById(userId).orElse(null);
        return jpaProductViews.findTop10ByUserOrderBySearchTimeDesc(user);
    }


    public List<ProductEntity> getTopViewedProducts() {
        return jpaProductViews.findTopViewedProducts();
    }
}
