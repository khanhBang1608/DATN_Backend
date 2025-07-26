package com.java.fashionshop.controller;

import com.java.fashionshop.bean.PromotionBean;
import com.java.fashionshop.dto.PromotionDTO;
import com.java.fashionshop.services.PromotionService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/promotions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManagePromotionController {

    @Autowired
    private PromotionService promotionService;

    @GetMapping
    public ResponseEntity<List<PromotionDTO>> getAllPromotions() {
        return ResponseEntity.ok(promotionService.findAllDto());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable Integer id) {
        PromotionDTO dto = promotionService.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.badRequest().body("Không tìm thấy khuyến mãi.");
    }

    @PostMapping
    public ResponseEntity<?> createPromotion(@Valid @RequestBody PromotionBean bean, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        if (bean.getStartDate().isAfter(bean.getEndDate())) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc.");
        }

        return ResponseEntity.ok(promotionService.save(bean));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePromotion(@PathVariable Integer id,
                                             @Valid @RequestBody PromotionBean bean,
                                             BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        if (bean.getStartDate().isAfter(bean.getEndDate())) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc.");
        }

        PromotionDTO updated = promotionService.update(id, bean);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.badRequest().body("Không tìm thấy khuyến mãi để cập nhật.");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePromotion(@PathVariable Integer id) {
        promotionService.delete(id);
        return ResponseEntity.ok("Đã xóa khuyến mãi.");
    }
}
