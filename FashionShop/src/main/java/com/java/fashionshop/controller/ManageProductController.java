package com.java.fashionshop.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.java.fashionshop.bean.ProductBean;
import com.java.fashionshop.bean.ProductVariantBean;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.services.CategoryService;
import com.java.fashionshop.services.ProductService;
import com.java.fashionshop.services.ProductVariantService;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManageProductController {
	
	 @Autowired
	    private CategoryService categoryService;
	 
	 @Autowired
	    private ProductService productService;

	 @Autowired
	    private JpaProduct jpaProduct;
	 
	 @Autowired
	 private ProductVariantService productVariantService;

	
	 @GetMapping("/products")
	 public List<ProductDTO> getAllProduct() {
	     return jpaProduct.findAll()
	                      .stream()
	                      .map(this::convertToDTO)
	                      .toList();
	 }
	 
	 @GetMapping("/products/{id}")
	 public ResponseEntity<?> getProductById(@PathVariable("id") Integer id) {
	     ProductEntity product = productService.findEntityById(id);
	     if (product == null) {
	         return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy sản phẩm");
	     }
	     return ResponseEntity.ok(convertToDTO(product));
	 }

	@PostMapping("/products")
	public ResponseEntity<?> addProduct(@RequestBody ProductBean productBean) {
	    try {
	        ProductEntity saved = productService.save(productBean);
	        return ResponseEntity.ok(convertToDTO(saved));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
	    }
	}

	@PutMapping("/products/{id}")
	public ResponseEntity<?> updateProduct(@PathVariable("id") Integer id, @RequestBody ProductBean productBean) {
	    try {
	        productBean.setProductId(id);
	        ProductEntity updated = productService.save(productBean);
	        return ResponseEntity.ok(convertToDTO(updated));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
	    }
	}
	
	@GetMapping("/products/{productId}/variants")
	public ResponseEntity<List<ProductVariantDTO>> getVariantsByProductId(@PathVariable Integer productId) {
	    List<ProductVariantEntity> variants = productVariantService.findEntityByProductId(productId);
	    List<ProductVariantDTO> dtos = variants.stream()
	            .map(this::convertToDTO)
	            .toList();
	    return ResponseEntity.ok(dtos);
	}
	
	@GetMapping("/product-variants/{id}")
	public ResponseEntity<?> getVariantById(@PathVariable Integer id) {
	    ProductVariantEntity entity = productVariantService.findEntityById(id);
	    if (entity == null) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy biến thể");
	    }
	    return ResponseEntity.ok(convertToDTO(entity));
	}
	
	@PostMapping("/product-variants")
	public ResponseEntity<?> addProductVariant(@ModelAttribute ProductVariantBean bean) {
	    try {
	        ProductVariantEntity saved = productVariantService.save(bean);
	        return ResponseEntity.ok(convertToDTO(saved));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
	    }
	}


	@PutMapping("/product-variants/{id}")
	public ResponseEntity<?> updateProductVariant(
	        @PathVariable Integer id,
	        @ModelAttribute ProductVariantBean bean
	) {
	    try {
	        ProductVariantEntity updated = productVariantService.update(id, bean);
	        return ResponseEntity.ok(convertToDTO(updated));
	    } catch (IllegalArgumentException e) {
	        return ResponseEntity.badRequest().body(e.getMessage());
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
	    }
	}


	public ProductDTO convertToDTO(ProductEntity product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus());
        dto.setDateCreated(product.getDateCreated());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
        }

        if (product.getVariants() != null) {
            List<ProductVariantDTO> variantDTOs = product.getVariants().stream().map(variant -> {
                ProductVariantDTO variantDTO = new ProductVariantDTO();
                variantDTO.setProductVariantId(variant.getProductVariantId());
                variantDTO.setStock(variant.getStock());
                variantDTO.setPrice(variant.getPrice());
                variantDTO.setImageName(variant.getImageName());

                if (variant.getColor() != null) {
                    variantDTO.setColorId(variant.getColor().getColorId());
                    variantDTO.setColorName(variant.getColor().getColorName());
                }

                if (variant.getSize() != null) {
                    variantDTO.setSizeId(variant.getSize().getSizeId());
                    variantDTO.setSizeName(variant.getSize().getSizeName());
                }

                return variantDTO;
            }).toList();

            dto.setVariants(variantDTOs);
        } else {
            // Nếu chưa có biến thể thì trả về danh sách rỗng
            dto.setVariants(List.of());
        }

        return dto;
    }
	
	private ProductVariantDTO convertToDTO(ProductVariantEntity variant) {
	    ProductVariantDTO dto = new ProductVariantDTO();
	    dto.setProductVariantId(variant.getProductVariantId());
	    dto.setStock(variant.getStock());
	    dto.setPrice(variant.getPrice());

	    if (variant.getColor() != null) {
	        dto.setColorId(variant.getColor().getColorId());
	        dto.setColorName(variant.getColor().getColorName());
	    }

	    if (variant.getSize() != null) {
	        dto.setSizeId(variant.getSize().getSizeId());
	        dto.setSizeName(variant.getSize().getSizeName());
	    }

	    if (variant.getImageName() != null) {
	        dto.setImageName(variant.getImageName());
	    }

	    return dto;
	}

}
