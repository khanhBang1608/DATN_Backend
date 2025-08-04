package com.java.fashionshop.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.RequestParam;

import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.services.OrderService;

@RestController
@RequestMapping("/api/public")
public class OrderControllerPublic {
	@Autowired
    private OrderService orderService;

	@GetMapping("/top50-products")
	public ResponseEntity<Page<ProductDTO>> getBestSellingProducts(
	        @RequestParam(defaultValue = "0") int page,
	        @RequestParam(defaultValue = "10") int size) {

	    Page<ProductDTO> products = orderService.getBestSellingProducts(PageRequest.of(page, size));
	    return ResponseEntity.ok(products);
	}
}
