package com.java.fashionshop.controller;

import com.java.fashionshop.bean.ProductPromotionBean;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.services.ProductPromotionService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product-promotions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManageProductPromotionController {

    @Autowired
    private ProductPromotionService service;

    @GetMapping
    public ResponseEntity<List<ProductPromotionDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ProductPromotionBean bean, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                .map(err -> err.getDefaultMessage())
                .toList();
            return ResponseEntity.badRequest().body(errors);
        }

        ProductPromotionDTO resultDTO = service.save(bean);
        if (resultDTO == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy biến thể sản phẩm hoặc khuyến mãi.");
        }

        return ResponseEntity.ok(resultDTO);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody ProductPromotionBean bean,
                                    BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(errors);
        }

        ProductPromotionDTO updated = service.update(id, bean);
        if (updated == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy liên kết sản phẩm - khuyến mãi để cập nhật.");
        }

        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xóa liên kết sản phẩm - khuyến mãi");
    }
}
