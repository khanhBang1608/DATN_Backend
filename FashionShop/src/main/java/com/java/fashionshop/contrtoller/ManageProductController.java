package com.java.fashionshop.contrtoller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.fashionshop.bean.ProductBean;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.services.CategoryService;
import com.java.fashionshop.services.ProductService;

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
	
	 @GetMapping("/products")
	 public List<ProductDTO> getAllProduct() {
	     return jpaProduct.findAll()
	                      .stream()
	                      .map(this::convertToDTO)
	                      .toList();
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

	private ProductDTO convertToDTO(ProductEntity product) {
	    ProductDTO dto = new ProductDTO();
	    dto.setProductId(product.getProductId());
	    dto.setName(product.getName());
	    dto.setStatus(product.getStatus());
	    dto.setDateCreated(product.getDateCreated());
	    dto.setDescription(product.getDescription());
	    dto.setCategoryId(product.getCategory().getCategoryId());
	    dto.setCategoryName(product.getCategory().getCategoryName());
	    return dto;
	}

}
