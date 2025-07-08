package com.java.fashionshop.services;

import com.java.fashionshop.bean.ProductVariantBean;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.jpa.JpaColors;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.jpa.JpaSizes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductVariantService {

    private static final String IMAGE_DIR = "images";

    @Autowired private JpaProductVariant jpaProductVariant;
    @Autowired private JpaProduct jpaProduct;
    @Autowired private JpaColors jpaColor;
    @Autowired private JpaSizes jpaSize;

    public List<ProductVariantBean> findByProductId(Integer productId) {
        return jpaProductVariant.findAll().stream()
                .filter(variant -> variant.getProduct().getProductId().equals(productId))
                .map(variant -> new ProductVariantBean(
                        variant.getProductVariantId(),
                        variant.getProduct().getProductId(),
                        variant.getColor().getColorId(),
                        variant.getColor().getColorName(),
                        variant.getSize() != null ? variant.getSize().getSizeId() : null,
                        variant.getSize() != null ? variant.getSize().getSizeName() : null,
                        variant.getPrice(),
                        variant.getStock(),
                        variant.getImageName()
                ))
                .collect(Collectors.toList());
    }

    public ProductVariantBean findById(Integer id) {
        Optional<ProductVariantEntity> opt = jpaProductVariant.findById(id);
        return opt.map(variant -> new ProductVariantBean(
                variant.getProductVariantId(),
                variant.getProduct().getProductId(),
                variant.getColor().getColorId(),
                variant.getColor().getColorName(),
                variant.getSize() != null ? variant.getSize().getSizeId() : null,
                variant.getSize() != null ? variant.getSize().getSizeName() : null,
                variant.getPrice(),
                variant.getStock(),
                variant.getImageName()
        )).orElse(null);
    }

    public ProductVariantEntity save(ProductVariantBean bean, MultipartFile file) {
        validateInput(bean);

        ProductVariantEntity entity = new ProductVariantEntity();
        entity.setProduct(jpaProduct.findById(bean.getProductId()).orElseThrow());
        entity.setColor(jpaColor.findById(bean.getColorId()).orElseThrow());
        if (bean.getSizeId() != null) {
            entity.setSize(jpaSize.findById(bean.getSizeId()).orElse(null));
        }

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(IMAGE_DIR, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                entity.setImageName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh: " + e.getMessage());
            }
        } else {
            entity.setImageName(bean.getImageName());
        }

        return jpaProductVariant.save(entity);
    }

    public ProductVariantEntity update(Integer variantId, ProductVariantBean bean, MultipartFile file) {
        ProductVariantEntity entity = jpaProductVariant.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể sản phẩm không tồn tại."));

        validateInput(bean);

        entity.setProduct(jpaProduct.findById(bean.getProductId()).orElseThrow());
        entity.setColor(jpaColor.findById(bean.getColorId()).orElseThrow());
        if (bean.getSizeId() != null) {
            entity.setSize(jpaSize.findById(bean.getSizeId()).orElse(null));
        } else {
            entity.setSize(null); // clear nếu không có size
        }

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(IMAGE_DIR, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, file.getBytes());
                entity.setImageName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh mới: " + e.getMessage());
            }
        }

        return jpaProductVariant.save(entity);
    }

    public void deleteById(Integer id) {
        jpaProductVariant.deleteById(id);
    }

    public List<ProductVariantEntity> findEntityByProductId(Integer productId) {
        return jpaProductVariant.findAll().stream()
                .filter(variant -> variant.getProduct().getProductId().equals(productId))
                .collect(Collectors.toList());
    }

    public ProductVariantEntity findEntityById(Integer id) {
        return jpaProductVariant.findById(id).orElse(null);
    }

    public void validateInput(ProductVariantBean bean) {
        if (bean.getProductId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn sản phẩm.");
        }

        if (bean.getColorId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn màu sắc.");
        }

        if (bean.getSizeId() != null && !jpaSize.existsById(bean.getSizeId())) {
            throw new IllegalArgumentException("Size không tồn tại.");
        }

        if (bean.getPrice() == null || bean.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Giá sản phẩm phải lớn hơn 0.");
        }

        if (bean.getStock() == null || bean.getStock() < 0) {
            throw new IllegalArgumentException("Số lượng tồn kho phải lớn hơn hoặc bằng 0.");
        }

        if (!jpaProduct.existsById(bean.getProductId())) {
            throw new IllegalArgumentException("Sản phẩm không tồn tại.");
        }

        if (!jpaColor.existsById(bean.getColorId())) {
            throw new IllegalArgumentException("Màu sắc không tồn tại.");
        }

        if (bean.getImageName() != null && bean.getImageName().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên ảnh không được để trống nếu đã cung cấp.");
        }
    }
}
