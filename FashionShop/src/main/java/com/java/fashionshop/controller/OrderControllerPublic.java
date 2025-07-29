package com.java.fashionshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.services.OrderService;

@RestController
@RequestMapping("/api/public")
public class OrderControllerPublic {
	@Autowired
    private OrderService orderService;

    @GetMapping("/top50-products")
    public ResponseEntity<List<ProductDTO>> getTop50BestSellingProducts() {
        List<ProductDTO> products = orderService.getTop50BestSellingProducts();
        return ResponseEntity.ok(products);
    }
}
