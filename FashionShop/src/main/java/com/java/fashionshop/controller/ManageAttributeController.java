package com.java.fashionshop.controller;

import com.java.fashionshop.bean.ColorBean;
import com.java.fashionshop.entity.ColorsEntity;
import com.java.fashionshop.entity.SizesEntity;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.services.AttributeService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/attributes")
public class ManageAttributeController {

    @Autowired
    private AttributeService attributeService;

    @Autowired
    private JpaProductVariant productVariantRepository;

    // -------- COLORS --------
    @GetMapping("/colors")
    public ResponseEntity<Page<ColorsEntity>> getColorsPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search // <- thêm dòng này
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ColorsEntity> result;

        if (search != null && !search.trim().isEmpty()) {
            result = attributeService.searchColorsByName(search.trim(), pageable);
        } else {
            result = attributeService.getAllColors(pageable);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/colors")
    public ResponseEntity<?> createColor(@RequestBody @Valid ColorBean colorBean, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }

        if (attributeService.existsColorName(colorBean.getColorName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Tên màu đã tồn tại.");
        }

        ColorsEntity color = new ColorsEntity();
        color.setColorName(colorBean.getColorName());
        return ResponseEntity.ok(attributeService.addColor(color));
    }
    
    @PutMapping("/colors/{id}")
    public ResponseEntity<?> updateColor(@PathVariable Integer id, @RequestBody @Valid ColorBean updatedBean, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getAllErrors().get(0).getDefaultMessage());
        }

        Optional<ColorsEntity> existing = attributeService.getColorById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ColorsEntity current = existing.get();
        if (!current.getColorName().equalsIgnoreCase(updatedBean.getColorName())
                && attributeService.existsColorName(updatedBean.getColorName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Tên màu đã tồn tại.");
        }

        current.setColorName(updatedBean.getColorName());
        return ResponseEntity.ok(attributeService.updateColor(id, current));
    }

    @DeleteMapping("/colors/{id}")
    public ResponseEntity<?> deleteColor(@PathVariable Integer id) {
        Optional<ColorsEntity> colorOpt = attributeService.getColorById(id);
        if (colorOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ColorsEntity color = colorOpt.get();
        if (productVariantRepository.existsByColor(color)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Không thể xóa màu này vì đang được sử dụng trong các biến thể sản phẩm.");
        }

        attributeService.deleteColor(color);
        return ResponseEntity.ok("Màu đã được xóa thành công.");
    }

    // -------- SIZES --------
    @GetMapping("/sizes")
    public ResponseEntity<Page<SizesEntity>> getSizesPaginated(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String search
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SizesEntity> result;

        if (search != null && !search.trim().isEmpty()) {
            result = attributeService.searchSizesByName(search.trim(), pageable);
        } else {
            result = attributeService.getAllSizes(pageable);
        }

        return ResponseEntity.ok(result);
    }


    @PostMapping("/sizes")
    public ResponseEntity<?> createSize(@RequestBody SizesEntity size) {
        if (size.getSizeName() == null || size.getSizeName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("⚠️ Tên size không được để trống.");
        }

        if (attributeService.existsSizeName(size.getSizeName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("⚠️ Tên size đã tồn tại.");
        }

        return ResponseEntity.ok(attributeService.addSize(size));
    }

    @PutMapping("/sizes/{id}")
    public ResponseEntity<?> updateSize(@PathVariable Integer id, @RequestBody SizesEntity updatedSize) {
        if (updatedSize.getSizeName() == null || updatedSize.getSizeName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Tên size không được để trống.");
        }

        Optional<SizesEntity> existing = attributeService.getSizeById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SizesEntity current = existing.get();

        if (!current.getSizeName().equalsIgnoreCase(updatedSize.getSizeName())
                && attributeService.existsSizeName(updatedSize.getSizeName())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("⚠️ Tên size đã tồn tại.");
        }

        return ResponseEntity.ok(attributeService.updateSize(id, updatedSize));
    }

    @DeleteMapping("/sizes/{id}")
    public ResponseEntity<?> deleteSize(@PathVariable Integer id) {
        Optional<SizesEntity> sizeOpt = attributeService.getSizeById(id);
        if (sizeOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        SizesEntity size = sizeOpt.get();
        if (productVariantRepository.existsBySize(size)) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("⚠️ Không thể xóa size này vì đang được sử dụng trong các biến thể sản phẩm.");
        }

        attributeService.deleteSize(size);
        return ResponseEntity.ok("✅ Size đã được xóa thành công.");
    }
}

