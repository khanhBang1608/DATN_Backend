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

        entity.setPrice(bean.getPrice());
        entity.setStock(bean.getStock());

        // Xử lý ảnh
        MultipartFile file = bean.getImage();
        if (file != null && !file.isEmpty()) {
            // Nếu có ảnh cũ thì xóa
            if (entity.getImageName() != null && !entity.getImageName().isEmpty()) {
                Path oldImagePath = Paths.get(IMAGE_DIR, entity.getImageName());
                try {
                    Files.deleteIfExists(oldImagePath);
                } catch (IOException e) {
                    System.err.println("Không thể xóa ảnh cũ: " + e.getMessage());
                    // Không throw lỗi để vẫn tiếp tục cập nhật ảnh mới
                }
            }

            // Lưu ảnh mới
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
        // Nếu không chọn ảnh mới thì giữ nguyên ảnh cũ (không thay đổi gì)

        return jpaProductVariant.save(entity);
    }

    public List<ProductVariantEntity> findEntityByProductId(Integer productId) {
        return jpaProductVariant.findAll().stream()
                .filter(variant -> variant.getProduct().getProductId().equals(productId))
                .collect(Collectors.toList());
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

        MultipartFile file = bean.getImage();
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn ảnh.");
        }
        if(file.getSize() > 1) {
        	throw new IllegalArgumentException("Chi duoc them 1 anh");
        }
    }
}
