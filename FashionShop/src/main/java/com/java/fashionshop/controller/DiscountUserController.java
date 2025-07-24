package com.java.fashionshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.services.DiscountService;

@RestController
@RequestMapping("/api/user")
public class DiscountUserController {

    @Autowired
    private DiscountService discountService;

    @GetMapping("/discount/findById/{id}")
    public DiscountEntity getById(@PathVariable Integer id) {
        return discountService.findById(id);
    }

    @PutMapping("/discount/update/{id}")
    public ResponseEntity<DiscountEntity> update(@PathVariable Integer id, @RequestBody DiscountBean bean) {
        return ResponseEntity.ok(discountService.update(id, bean));
    }
    @GetMapping("/discount/available")
    public ResponseEntity<List<DiscountEntity>> getAvailableDiscounts() {
        return ResponseEntity.ok(discountService.getAvailableDiscounts());
    }
    
    
}
