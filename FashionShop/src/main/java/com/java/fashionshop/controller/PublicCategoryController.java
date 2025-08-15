package com.java.fashionshop.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.dto.CategoryDTO;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;

@RestController
@RequestMapping("/api/public/category")
public class PublicCategoryController {

    @Autowired
    private JpaCategory categoryJPA;

    @GetMapping("/categories")
    public List<CategoryDTO> getPublicCategories() {
        // Lấy tất cả danh mục active
        List<CategoryEntity> categories = categoryJPA.findAll().stream()
                .filter(CategoryEntity::isStatus) // Chỉ lấy status = true
                .collect(Collectors.toList());

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

        // Xây dựng cây danh mục và lọc các cha không có con
        List<CategoryDTO> rootCategories = new ArrayList<>();
        for (CategoryDTO dto : dtoMap.values()) {
            Integer parentId = dto.getParentId();
            if (parentId == null) {
                rootCategories.add(dto); // Thêm danh mục gốc
            } else {
                CategoryDTO parent = dtoMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(dto); // Thêm con vào cha
                }
            }
        }

        // Lọc các danh mục cha không có con
        return rootCategories.stream()
                .filter(root -> !root.getChildren().isEmpty()) // Chỉ giữ cha có con
                .collect(Collectors.toList());
    }}