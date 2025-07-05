package com.java.fashionshop.services;

import com.java.fashionshop.bean.ProductVariantBean;
import com.java.fashionshop.entity.ColorsEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.SizesEntity;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaProductVariant;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductVariantService {

    @Autowired
    private JpaProductVariant jpaProductVariant;

    @Autowired
    private JpaProduct jpaProduct;

    public List<ProductVariantBean> findByProductId(Integer productId) {
        return jpaProductVariant.findAll().stream()
                .filter(variant -> variant.getProduct().getProductId().equals(productId))
                .map(variant -> new ProductVariantBean(
                	    variant.getProductVariantId(),
                	    variant.getProduct().getProductId(),
                	    variant.getColor().getColorId(),
                	    variant.getColor().getColorName(),
                	    variant.getSize().getSizeId(),
                	    variant.getSize().getSizeName(),
                	    variant.getPrice(),
                	    variant.getStock()
                	))
                .collect(Collectors.toList());
    }

    public ProductVariantBean findById(Integer id) {
        Optional<ProductVariantEntity> variantOpt = jpaProductVariant.findById(id);
        return variantOpt.map(variant -> new ProductVariantBean(
                variant.getProductVariantId(),
                variant.getProduct().getProductId(),
                variant.getColor() != null ? variant.getColor().getColorId() : null,
                variant.getColor() != null ? variant.getColor().getColorName() : null,
                variant.getSize() != null ? variant.getSize().getSizeId() : null,
                variant.getSize() != null ? variant.getSize().getSizeName() : null,
                variant.getPrice(),
                variant.getStock()
        )).orElse(null);
    }

    @Transactional
    public ProductVariantEntity save(ProductVariantBean bean) {
        ProductVariantEntity entity = new ProductVariantEntity();

        if (bean.getVariantId() != null) {
            entity = jpaProductVariant.findById(bean.getVariantId()).orElse(new ProductVariantEntity());
        }

        ProductEntity product = jpaProduct.findById(bean.getProductId())
                .orElseThrow(() -> new RuntimeException("Product not found"));

        entity.setProduct(product);
        ColorsEntity color = new ColorsEntity();
        color.setColorId(bean.getColorId());
        entity.setColor(color);

        SizesEntity size = new SizesEntity();
        size.setSizeId(bean.getSizeId());
        entity.setSize(size);

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        return jpaProductVariant.save(entity);
    }

    public void deleteById(Integer id) {
        jpaProductVariant.deleteById(id);
    }
}
