package com.java.fashionshop.controller;

import com.java.fashionshop.bean.ProductPromotionBean;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.ProductPromotionEntity;
import com.java.fashionshop.services.ProductPromotionService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/admin/product-promotions")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
@Validated
public class ManageProductPromotionController {

    @Autowired
    private ProductPromotionService service;

    @GetMapping("/{id}")
    public ResponseEntity<ProductPromotionDTO> getById(@PathVariable Integer id) {
        ProductPromotionEntity entity = service.findById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(service.convertToDTO(entity));
    }

    @GetMapping("/promotion/{promotionId}")
    public ResponseEntity<List<ProductPromotionDTO>> getByPromotionId(@PathVariable Integer promotionId) {
        List<ProductPromotionDTO> list = service.findByPromotionId(promotionId);
        return ResponseEntity.ok(list);
    }

    @GetMapping
    public ResponseEntity<List<ProductPromotionDTO>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/productVariants")
    public List<ProductVariantDTO> getVariantsByProductId(@RequestParam Integer productId) {
        return service.getVariantsByProductId(productId);
    }

    @PostMapping
    public ResponseEntity<?> createBulk(@RequestBody List<@Valid ProductPromotionBean> beans, BindingResult result) {
        List<String> allErrors = new ArrayList<>();

        for (int i = 0; i < beans.size(); i++) {
            ProductPromotionBean bean = beans.get(i);
//            if (bean.getQuantityLimit() == null || bean.getQuantityLimit() <= 0) {
//                allErrors.add("Mục " + (i + 1) + ": Số lượng giới hạn phải lớn hơn 0");
//            }
            if (bean.getPromotionId() == null) {
                allErrors.add("Mục " + (i + 1) + ": Mã khuyến mãi không được để trống");
            }
            if (bean.getProductVariantId() == null) {
                allErrors.add("Mục " + (i + 1) + ": Biến thể sản phẩm không được để trống");
            }
        }

        if (!allErrors.isEmpty()) {
            return ResponseEntity.badRequest().body(allErrors);
        }

        List<ProductPromotionDTO> resultList = service.saveBulk(null, beans);
        return ResponseEntity.ok(resultList);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id,
                                    @Valid @RequestBody ProductPromotionBean bean,
                                    BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(e -> e.getDefaultMessage())
                    .toList();
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
