package com.java.fashionshop.controller;

import com.java.fashionshop.bean.DiscountBean;
import com.java.fashionshop.dto.DiscountDTO;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.services.DiscountService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class DiscountController {

    @Autowired
    private DiscountService discountService;

    @GetMapping("/discount/findAll")
    public List<DiscountDTO> getAll() {
        return discountService.getAllDTOs();
    }

    @GetMapping("/discount/findById/{id}")
    public ResponseEntity<DiscountDTO> getById(@PathVariable Integer id) {
        DiscountEntity entity = discountService.findById(id);
        if (entity != null) {
            return ResponseEntity.ok(discountService.convertToDTO(entity));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/discount/create")
    public ResponseEntity<?> create(@Valid @RequestBody DiscountBean bean, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        return ResponseEntity.ok(discountService.save(bean));
    }

    @PutMapping("/discount/update/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody DiscountBean bean, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        return ResponseEntity.ok(discountService.update(id, bean));
    }

    @DeleteMapping("/discount/delete/{id}")
    public void delete(@PathVariable Integer id) {
        discountService.delete(id);
    }
}
