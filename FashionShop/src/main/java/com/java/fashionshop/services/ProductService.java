package com.java.fashionshop.services;

import com.java.fashionshop.bean.ProductBean;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaProduct;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private JpaCategory jpaCategory;

    public List<ProductBean> findAll() {
        List<ProductEntity> products = jpaProduct.findAll();
        List<ProductBean> result = new ArrayList<>();

        for (ProductEntity entity : products) {
            ProductBean bean = new ProductBean();
            bean.setProductId(entity.getProductId());
            bean.setName(entity.getName());
            bean.setDescription(entity.getDescription());
            bean.setStatus(entity.getStatus());
            bean.setCategoryId(entity.getCategory().getCategoryId());
            result.add(bean);
        }

        return result;
    }

    public ProductBean findById(Integer id) {
        return jpaProduct.findById(id).map(entity -> {
            ProductBean bean = new ProductBean();
            bean.setProductId(entity.getProductId());
            bean.setName(entity.getName());
            bean.setDescription(entity.getDescription());
            bean.setStatus(entity.getStatus());
            bean.setCategoryId(entity.getCategory().getCategoryId());
            return bean;
        }).orElse(null);
    }

    @Transactional
    public ProductEntity save(ProductBean bean) {
        ProductEntity entity = new ProductEntity();
        if (bean.getProductId() != null) {
            entity = jpaProduct.findById(bean.getProductId()).orElse(new ProductEntity());
        }

        entity.setName(bean.getName());
        entity.setDescription(bean.getDescription());
        entity.setStatus(bean.getStatus());

        CategoryEntity category = jpaCategory.findById(bean.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        entity.setCategory(category);

        return jpaProduct.save(entity);
    }

    public void deleteById(Integer id) {
        jpaProduct.deleteById(id);
    }
}
