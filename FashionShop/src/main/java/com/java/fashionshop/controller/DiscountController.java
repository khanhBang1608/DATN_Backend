package com.java.fashionshop.controller;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.services.DiscountService;

import org.springframework.beans.factory.annotation.Autowired;
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
    public DiscountEntity create(@RequestBody DiscountBean bean) {
        return discountService.save(bean);
    }

    @PutMapping("/discount/update/{id}")
    public DiscountEntity update(@PathVariable Integer id, @RequestBody DiscountBean bean) {
        return discountService.update(id, bean);
    }

    @DeleteMapping("/discount/delete/{id}")
    public void delete(@PathVariable Integer id) {
        discountService.delete(id);
    }
}
