package com.java.fashionshop.controller;

import com.java.fashionshop.dto.ColorsDTO;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductOptionsResponse;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.dto.SizesDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.services.ProductService;
import com.java.fashionshop.services.ProductVariantService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductClientController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;

    // ✅ 1. Lấy toàn bộ sản phẩm cho trang danh sách
    @GetMapping("/products")
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductEntity> products = productService.getAllEntity();
        List<ProductDTO> result = products.stream()
                .map(productService::convertToDTO)  // tái sử dụng convertToDTO
                .toList();
        return ResponseEntity.ok(result);
    }

    // ✅ 2. Lấy chi tiết sản phẩm theo id
    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer id) {
        ProductEntity entity = productService.findEntityById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productService.convertToDTO(entity));  // tái sử dụng convertToDTO
    }

    // ✅ 3. Lấy danh sách biến thể theo sản phẩm
    @GetMapping("/products/{id}/variants")
    public ResponseEntity<List<ProductVariantDTO>> getVariants(@PathVariable Integer id) {
        List<ProductVariantEntity> variants = productVariantService.findEntityByProductId(id);
        List<ProductVariantDTO> result = variants.stream()
                .map(this::convertToVariantDTO)
                .toList();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/products/{productId}/variant")
    public ResponseEntity<?> getVariantByColorAndSize(
            @PathVariable Integer productId,
            @RequestParam Integer colorId,
            @RequestParam(required = false) Integer sizeId) {

        ProductVariantEntity variant = productVariantService.findByProductIdAndColorAndSize(productId, colorId, sizeId);

        if (variant == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(convertToVariantDTO(variant));
    }
    
    @GetMapping("/products/{id}/options")
    public ResponseEntity<?> getProductOptions(@PathVariable Integer id) {
        List<ProductVariantEntity> variants = productVariantService.findEntityByProductId(id);

        // Lấy các màu không trùng
        List<ColorsDTO> colors = variants.stream()
                .filter(v -> v.getColor() != null)
                .map(v -> new ColorsDTO(v.getColor().getColorId(), v.getColor().getColorName()))
                .distinct()
                .toList();

        // Lấy các size không trùng
        List<SizesDTO> sizes = variants.stream()
                .filter(v -> v.getSize() != null)
                .map(v -> new SizesDTO(v.getSize().getSizeId(), v.getSize().getSizeName()))
                .distinct()
                .toList();

        ProductOptionsResponse response = new ProductOptionsResponse(colors, sizes);
        return ResponseEntity.ok(response);
    }


    private ProductVariantDTO convertToVariantDTO(ProductVariantEntity variant) {
        ProductVariantDTO dto = new ProductVariantDTO();
        dto.setProductVariantId(variant.getProductVariantId());
        dto.setPrice(variant.getPrice());
        dto.setStock(variant.getStock());

        if (variant.getColor() != null) {
            dto.setColorId(variant.getColor().getColorId());
            dto.setColorName(variant.getColor().getColorName());
        }

        if (variant.getSize() != null) {
            dto.setSizeId(variant.getSize().getSizeId());
            dto.setSizeName(variant.getSize().getSizeName());
        }

        dto.setImageName(variant.getImageName());
        return dto;
    }
}
