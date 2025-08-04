package com.java.fashionshop.controller;

import com.java.fashionshop.dto.ColorsDTO;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductOptionsResponse;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.dto.SizesDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.services.FavoriteService;
import com.java.fashionshop.services.OrderService;
import com.java.fashionshop.services.ProductService;
import com.java.fashionshop.services.ProductVariantService;

import com.java.fashionshop.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;


import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProductClientController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantService productVariantService;
    
    @Autowired
    private OrderService orderService;
    
    @Autowired
    private FavoriteService favoriteService;
    
    @GetMapping("/products/top10")
    public ResponseEntity<?> getTopNewestProductsWithVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductEntity> products = productService.findTopNewestProductsWithVariants(pageable);

        Page<ProductDTO> result = products.map(productService::convertToDTO);

        return ResponseEntity.ok(result);
    }

    
    @GetMapping("/products")
    public ResponseEntity<?> getAllProducts(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {

        if (page != null && size != null) {
            Pageable pageable = PageRequest.of(page, size);
            Page<ProductDTO> pagedResult = productService.getPaginatedProducts(pageable);
            return ResponseEntity.ok(pagedResult);
        } else {
            List<ProductDTO> dtos = productService.getAllEntity().stream()
                    .filter(p -> p.getVariants() != null && !p.getVariants().isEmpty())
                    .map(productService::convertToDTO)
                    .toList();

            return ResponseEntity.ok(dtos);
        }
    }


    @GetMapping("/products/{id}")
    public ResponseEntity<?> getProductDetail(@PathVariable Integer id) {
        ProductEntity entity = productService.findEntityById(id);
        if (entity == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(productService.convertToDTO(entity));  // tái sử dụng convertToDTO
    }

    @GetMapping("/products/{id}/variants")
    public ResponseEntity<List<ProductVariantDTO>> getVariants(@PathVariable Integer id) {
        List<ProductVariantEntity> variants = productVariantService.findEntityByProductId(id);
        List<ProductVariantDTO> result = variants.stream()
                .map(this::convertToVariantDTO)
                .toList();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/variants/{variantId}/product-id")
    public ResponseEntity<?> getProductIdByVariantId(@PathVariable Integer variantId) {
        ProductVariantEntity variant = productVariantService.findEntityById(variantId);
        if (variant == null || variant.getProduct() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().body(Map.of("productId", variant.getProduct().getProductId()));
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
                .map(v -> new ColorsDTO())
                .distinct()
                .toList();

        // Lấy các size không trùng
        List<SizesDTO> sizes = variants.stream()
                .filter(v -> v.getSize() != null)
                .map(v -> new SizesDTO())
                .distinct()
                .toList();

        ProductOptionsResponse response = new ProductOptionsResponse(colors, sizes);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/products/related")
    public ResponseEntity<Page<ProductDTO>> getRelatedProducts(
            @RequestParam Integer categoryId,
            @RequestParam(required = false) Integer excludeProductId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductDTO> pagedResult = productService.getRelatedProducts(categoryId, excludeProductId, pageable);

        return ResponseEntity.ok(pagedResult);
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
    
    @GetMapping("/products/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String keyword) {
        List<ProductDTO> results = productService.searchProductsByName(keyword);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/products/{productId}/average-rating")
    public ResponseEntity<Double> getAverageRating(@PathVariable Integer productId) {
        Double average = reviewService.getAverageRatingByProductId(productId);
        return ResponseEntity.ok(average != null ? average : 0.0);
    }
    
    @GetMapping("/products/{productId}/sold-count")
    public ResponseEntity<?> getSoldCountByProductId(@PathVariable Integer productId) {
        try {
            Long soldCount = orderService.getTotalSoldQuantityByProductId(productId);
            return ResponseEntity.ok().body(Map.of("soldCount", soldCount));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể lấy số lượng đã bán"));
        }
    }
    
    @GetMapping("/products/{productId}/favorite-count")
    public ResponseEntity<?> getFavoriteCount(@PathVariable Integer productId) {
        try {
            Long count = favoriteService.getFavoriteCountByProductId(productId);
            return ResponseEntity.ok(Map.of("favoriteCount", count));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Không thể lấy lượt yêu thích"));
        }
    }

}
