package com.java.fashionshop.controller;

import com.java.fashionshop.bean.PromotionBean;
import com.java.fashionshop.dto.PromotionDTO;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.services.PromotionService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManagePromotionController {

    @Autowired
    private PromotionService service;

    @GetMapping
    public ResponseEntity<List<PromotionsEntity>> getAll() {
        return ResponseEntity.ok(service.findAlll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        PromotionDTO dto = service.findById(id);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody PromotionBean bean, BindingResult result) {
        // Kiểm tra lỗi validation từ @Valid
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .toList(); // hoặc .collect(Collectors.toList()) nếu bạn dùng Java < 16
            return ResponseEntity.badRequest().body(errors);
        }

        // Kiểm tra logic ngày tháng
        if (bean.getStartDate().isAfter(bean.getEndDate())) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc.");
        }

        return ResponseEntity.ok(service.save(bean));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody PromotionBean bean,
                                    BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage()).toList();
            return ResponseEntity.badRequest().body(errors);
        }

        if (bean.getStartDate().isAfter(bean.getEndDate())) {
            return ResponseEntity.badRequest().body("Ngày bắt đầu phải trước ngày kết thúc.");
        }

        PromotionDTO updated = service.update(id, bean);
        if (updated == null) {
            return ResponseEntity.badRequest().body("Không tìm thấy khuyến mãi cần cập nhật.");
        }

        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.ok("Đã xóa khuyến mãi");
    }
}
