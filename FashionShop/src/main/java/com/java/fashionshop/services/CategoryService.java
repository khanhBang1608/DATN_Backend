package com.java.fashionshop.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.CategoryBean;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;

@Service
public class CategoryService {

    @Autowired
    private JpaCategory categoryJPA;

    // Tạo mới danh mục
    public void createCategory(CategoryBean bean) throws IllegalArgumentException {
        // Kiểm tra trùng tên khi tạo mới
        Optional<CategoryEntity> existingCategory = categoryJPA.findByCategoryName(bean.getName());
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setCategoryName(bean.getName());
        entity.setStatus(bean.getStatus());

        // Thiết lập danh mục cha nếu có
        if (bean.getParentId() != null) {
            CategoryEntity parent = categoryJPA.findById(bean.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục cha không tồn tại"));
            entity.setParent(parent);
        }

        categoryJPA.save(entity);
    }

    // Cập nhật danh mục
    public void updateCategory(Integer id, CategoryBean bean) throws IllegalArgumentException {
        Optional<CategoryEntity> optionalCategory = categoryJPA.findById(id);

        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }

        // Kiểm tra tên có bị trùng với danh mục khác không
        Optional<CategoryEntity> categoryByName = categoryJPA.findByCategoryName(bean.getName());
        if (categoryByName.isPresent() && !categoryByName.get().getCategoryId().equals(id)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        CategoryEntity entity = optionalCategory.get();
        entity.setCategoryName(bean.getName());
        entity.setStatus(bean.getStatus());

        // Cập nhật danh mục cha
        if (bean.getParentId() != null) {
            if (bean.getParentId().equals(id)) {
                throw new IllegalArgumentException("Không thể chọn chính nó làm danh mục cha");
            }
            CategoryEntity parent = categoryJPA.findById(bean.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục cha không tồn tại"));
            entity.setParent(parent);
        } else {
            entity.setParent(null); // Xóa danh mục cha nếu không chọn
        }

        categoryJPA.save(entity);
    }

    // Lấy một danh mục theo ID
    public CategoryEntity getCategoryById(Integer id) {
        return categoryJPA.findById(id).orElse(null);
    }
}
