package com.java.fashionshop.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.bean.CategoryBean;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CategoryService {

    private static final Logger logger = LoggerFactory.getLogger(CategoryService.class);

    @Autowired
    private JpaCategory categoryJPA;

    // Tạo mới danh mục
    public void createCategory(CategoryBean bean) throws IllegalArgumentException {
        logger.info("Creating category with name: '{}'", bean.getName());
        Optional<CategoryEntity> existingCategory = categoryJPA.findByCategoryName(bean.getName());
        if (existingCategory.isPresent()) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        CategoryEntity entity = new CategoryEntity();
        entity.setCategoryName(bean.getName().trim()); // Loại bỏ khoảng trắng thừa
        entity.setStatus(bean.getStatus());

        if (bean.getParentId() != null) {
            CategoryEntity parent = categoryJPA.findById(bean.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục cha không tồn tại"));
            entity.setParent(parent);
        }

        categoryJPA.save(entity);
    }

    // Cập nhật danh mục
    public void updateCategory(Integer id, CategoryBean bean) throws IllegalArgumentException {
        logger.info("Updating category with id: {} and name: '{}'", id, bean.getName());
        Optional<CategoryEntity> optionalCategory = categoryJPA.findById(id);

        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("Danh mục không tồn tại");
        }

        Optional<CategoryEntity> categoryByName = categoryJPA.findByCategoryName(bean.getName());
        if (categoryByName.isPresent() && !categoryByName.get().getCategoryId().equals(id)) {
            throw new IllegalArgumentException("Tên danh mục đã tồn tại");
        }

        CategoryEntity entity = optionalCategory.get();
        entity.setCategoryName(bean.getName().trim()); // Loại bỏ khoảng trắng thừa
        entity.setStatus(bean.getStatus());

        if (bean.getParentId() != null) {
            if (bean.getParentId().equals(id)) {
                throw new IllegalArgumentException("Không thể chọn chính nó làm danh mục cha");
            }
            CategoryEntity parent = categoryJPA.findById(bean.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục cha không tồn tại"));
            entity.setParent(parent);
        } else {
            entity.setParent(null);
        }

        categoryJPA.save(entity);
    }

    // Lấy một danh mục theo ID
    public CategoryEntity getCategoryById(Integer id) {
        return categoryJPA.findById(id).orElse(null);
    }
}