package com.java.fashionshop.controller;

import com.java.fashionshop.dto.PromotionDTO;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.services.PromotionService;
import com.java.fashionshop.services.ProductPromotionService;
import com.java.fashionshop.jpa.JpaPromotions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/promotions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class PromotionClientController {

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private ProductPromotionService productPromotionService;

    @Autowired
    private JpaPromotions promotionRepo;
    /**
     * Lấy danh sách khuyến mãi đang diễn ra (có status = true và nằm trong thời gian hợp lệ)
     */
    @GetMapping("/active")
    public ResponseEntity<List<PromotionDTO>> getActivePromotions() {
        LocalDate today = LocalDate.now(); // ✅ sửa từ LocalDateTime -> LocalDate

        List<PromotionDTO> list = promotionService.findAll().stream()
            .filter(p -> Boolean.TRUE.equals(p.getStatus()) &&
                         !p.getStartDate().isAfter(today) &&
                         !p.getEndDate().isBefore(today))
            .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }

    /**
     * Lấy chi tiết khuyến mãi theo ID, bao gồm các sản phẩm áp dụng
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getPromotionById(@PathVariable Integer id) {
        PromotionsEntity entity = promotionRepo.findById(id).orElse(null);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }

        PromotionDTO dto = promotionService.findById(id);
        // Set danh sách sản phẩm áp dụng
        if (entity.getProductPromotions() != null) {
            List<ProductPromotionDTO> productPromotions = entity.getProductPromotions()
                .stream()
                .map(productPromotionService::convertToDTO)
                .collect(Collectors.toList());
            dto.setProductPromotions(productPromotions);
        }

        return ResponseEntity.ok(dto);
    }

    /**
     * Lấy danh sách khuyến mãi áp dụng theo product variant
     */
    @GetMapping("/product-variant/{variantId}")
    public ResponseEntity<List<ProductPromotionDTO>> getPromotionByVariant(@PathVariable Integer variantId) {
        List<ProductPromotionDTO> list = productPromotionService.findAll().stream()
                .filter(p -> p.getProductVariantId().equals(variantId))
                .collect(Collectors.toList());

        return ResponseEntity.ok(list);
    }
}
