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
        return roots;
    }
}