package com.java.fashionshop.controller;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.services.DiscountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @GetMapping("/discount/findAll")
    public List<DiscountEntity> getAll() {
        return discountService.findAll();
    }

    @GetMapping("/discount/findById/{id}")
    public DiscountEntity getById(@PathVariable Integer id) {
        return discountService.findById(id);
    }

    @PostMapping("/discount/create")
    public ResponseEntity<DiscountEntity> create(@RequestBody DiscountBean bean) {
        return ResponseEntity.ok(discountService.save(bean));
    }

    @PutMapping("/discount/update/{id}")
    public ResponseEntity<DiscountEntity> update(@PathVariable Integer id, @RequestBody DiscountBean bean) {
        return ResponseEntity.ok(discountService.update(id, bean));
    }



    @DeleteMapping("/discount/delete/{id}")
    public void delete(@PathVariable Integer id) {
        discountService.delete(id);
    }
}
