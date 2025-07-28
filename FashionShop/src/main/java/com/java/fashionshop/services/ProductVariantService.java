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
                        null // Không trả ảnh dạng MultipartFile
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
                null
        )).orElse(null);
    }

    public ProductVariantEntity save(ProductVariantBean bean) {
        validateInput(bean);

        // Kiểm tra biến thể sản phẩm đã tồn tại hay chưa
        boolean exists = jpaProductVariant.findAll().stream().anyMatch(variant ->
            variant.getProduct().getProductId().equals(bean.getProductId()) &&
            variant.getColor().getColorId().equals(bean.getColorId()) &&
            ((variant.getSize() == null && bean.getSizeId() == null) ||   // cả hai đều null
             (variant.getSize() != null && variant.getSize().getSizeId().equals(bean.getSizeId()))) // hoặc cùng sizeId
        );

        if (exists) {
            throw new IllegalArgumentException("Biến thể sản phẩm đã tồn tại với cùng màu sắc và size.");
        }

        ProductVariantEntity entity = new ProductVariantEntity();
        entity.setProduct(jpaProduct.findById(bean.getProductId()).orElseThrow());
        entity.setColor(jpaColor.findById(bean.getColorId()).orElseThrow());

        if (bean.getSizeId() != null) {
            entity.setSize(jpaSize.findById(bean.getSizeId()).orElse(null));
        }

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        MultipartFile file = bean.getImage();
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
        }

        return jpaProductVariant.save(entity);
    }


    public ProductVariantEntity update(Integer variantId, ProductVariantBean bean) {
        ProductVariantEntity entity = jpaProductVariant.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể sản phẩm không tồn tại."));

        // Giữ nguyên color & size nếu không có thay đổi
        if (bean.getColorId() != null) {
            entity.setColor(jpaColor.findById(bean.getColorId())
                    .orElseThrow(() -> new IllegalArgumentException("Màu sắc không tồn tại.")));
        }

        if (bean.getSizeId() != null) {
            entity.setSize(jpaSize.findById(bean.getSizeId())
                    .orElseThrow(() -> new IllegalArgumentException("Size không tồn tại.")));
        }

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        MultipartFile file = bean.getImage();
        if (file != null && !file.isEmpty()) {
            // Xóa ảnh cũ nếu có
            if (entity.getImageName() != null && !entity.getImageName().isEmpty()) {
                Path oldImagePath = Paths.get(IMAGE_DIR, entity.getImageName());
                try {
                    Files.deleteIfExists(oldImagePath);
                } catch (IOException e) {
                    System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                }
            }

            try {
                String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path newPath = Paths.get(IMAGE_DIR, fileName);
                Files.createDirectories(newPath.getParent());
                Files.write(newPath, file.getBytes());
                entity.setImageName(fileName);
            } catch (IOException e) {
                throw new RuntimeException("Không thể lưu ảnh mới: " + e.getMessage());
            }
        }

        return jpaProductVariant.save(entity);
    }

    
    public void deleteById(Integer id) {
        ProductVariantEntity entity = jpaProductVariant.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Biến thể không tồn tại"));

        // Xóa ảnh nếu có
        if (entity.getImageName() != null && !entity.getImageName().isEmpty()) {
            Path imagePath = Paths.get(IMAGE_DIR, entity.getImageName());
            try {
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                System.err.println("Không thể xóa ảnh: " + e.getMessage());
            }
        }

        jpaProductVariant.deleteById(id);
    }


    public List<ProductVariantEntity> findEntityByProductId(Integer productId) {
        return jpaProductVariant.findByProduct_ProductId(productId);
    }

    public ProductVariantEntity findEntityById(Integer id) {
        return jpaProductVariant.findById(id).orElse(null);
    }
    
    public ProductVariantEntity findByProductIdAndColorAndSize(Integer productId, Integer colorId, Integer sizeId) {
        return jpaProductVariant.findAll().stream()
                .filter(variant ->
                        variant.getProduct().getProductId().equals(productId) &&
                        variant.getColor().getColorId().equals(colorId) &&
                        ((sizeId == null && variant.getSize() == null) || 
                         (variant.getSize() != null && variant.getSize().getSizeId().equals(sizeId)))
                )
                .findFirst()
                .orElse(null);
    }
}
