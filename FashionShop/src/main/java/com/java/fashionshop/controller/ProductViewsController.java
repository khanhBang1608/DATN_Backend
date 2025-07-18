package com.java.fashionshop.controller;

import com.java.fashionshop.entity.ProductViewsEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.services.ProductViewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class ProductViewsController {

    @Autowired
    private ProductViewsService productViewsService;

    // 1. Ghi nhận lượt xem
    @PostMapping("/product-views/record")
    public void recordView(@RequestParam Integer productId, @RequestParam Integer userId) {
        productViewsService.recordView(productId, userId);
    }

    // 2. Lấy các sản phẩm đã xem gần đây
    @GetMapping("/product-views/recent")
    public List<ProductViewsEntity> getRecentViews(@RequestParam Integer userId) {
        return productViewsService.getRecentViews(userId);
    }

    // 3. Lấy danh sách sản phẩm được xem nhiều nhất
    @GetMapping("/product-views/top")
    public List<ProductEntity> getTopViewedProducts() {
        return productViewsService.getTopViewedProducts();
    }
}
