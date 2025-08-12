package com.java.fashionshop.controller;
	
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
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

    @Transactional(readOnly = true) // Thêm để tránh LazyInitializationException khi truy cập products
    @GetMapping
    public List<CategoryDTO> getAllCategories() {
        List<CategoryEntity> categories = categoryJPA.findAll();

        // Tạo map từ ID đến Entity để dễ truy cập products
        Map<Integer, CategoryEntity> entityMap = categories.stream()
                .collect(Collectors.toMap(CategoryEntity::getCategoryId, entity -> entity));

        // Chuyển về DTO map để xử lý phân cấp
        Map<Integer, CategoryDTO> dtoMap = new HashMap<>();

        // Tạo tất cả DTO trước và gán parentId
        for (CategoryEntity category : categories) {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getCategoryId());
            dto.setCategoryName(category.getCategoryName());
            dto.setStatus(category.isStatus());
            dto.setParentId(category.getParent() != null ? category.getParent().getCategoryId() : null);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(dto.getCategoryId(), dto);
        }

        // Gán danh mục con cho cha
        List<CategoryDTO> roots = new ArrayList<>();
        for (CategoryDTO dto : dtoMap.values()) {
            if (dto.getParentId() == null) {
                roots.add(dto); // là danh mục gốc
            } else {
                CategoryDTO parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        // Tính toán productCount đệ quy cho từng root
        for (CategoryDTO root : roots) {
            calculateProductCount(root, entityMap);
        }

        return roots;
    }

    // Method helper để tính productCount đệ quy
    private void calculateProductCount(CategoryDTO dto, Map<Integer, CategoryEntity> entityMap) {
        // Lấy số sản phẩm trực tiếp của danh mục này
        CategoryEntity entity = entityMap.get(dto.getCategoryId());
        int count = (entity != null && entity.getProducts() != null) ? entity.getProducts().size() : 0;

        // Nếu có con, tính đệ quy và cộng tổng từ con
        for (CategoryDTO child : dto.getChildren()) {
            calculateProductCount(child, entityMap);
            count += child.getProductCount();
        }

        // Gán giá trị vào DTO
        dto.setProductCount(count);
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