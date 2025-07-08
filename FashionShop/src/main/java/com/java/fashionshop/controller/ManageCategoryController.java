package com.java.fashionshop.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.java.fashionshop.bean.CategoryBean;
import com.java.fashionshop.dto.CategoryDTO;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.services.CategoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/category")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManageCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private JpaCategory categoryJPA;

    // Lấy tất cả danh mục
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        return categoryJPA.findAll().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getCategoryId());
            dto.setCategoryName(category.getCategoryName());
            dto.setStatus(category.isStatus());
            if (category.getParent() != null) {
                dto.setParentId(category.getParent().getCategoryId());
            }
            return dto;
        }).toList();
    }

    // Lấy chi tiết danh mục theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Integer id) {
        CategoryEntity category = categoryService.getCategoryById(id);
        if (category == null) {
            return ResponseEntity.badRequest().body("Danh mục không tồn tại");
        }
        return ResponseEntity.ok(category);
    }

    // Tạo mới danh mục
    @PostMapping("/create")
    public ResponseEntity<?> createCategory(@Valid @RequestBody CategoryBean bean, BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage()).collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            categoryService.createCategory(bean);
            return ResponseEntity.ok("Tạo danh mục thành công");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    // Cập nhật danh mục
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateCategory(@PathVariable Integer id,
                                            @Valid @RequestBody CategoryBean bean,
                                            BindingResult result) {
        if (result.hasErrors()) {
            List<String> errors = result.getFieldErrors().stream()
                    .map(err -> err.getDefaultMessage()).collect(Collectors.toList());
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            categoryService.updateCategory(id, bean);
            return ResponseEntity.ok("Cập nhật danh mục thành công");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}