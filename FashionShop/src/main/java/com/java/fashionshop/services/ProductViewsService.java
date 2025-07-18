package com.java.fashionshop.services;

import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductViewDTO;
import com.java.fashionshop.dto.UserDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.entity.ProductViewsEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaProductViews;
import com.java.fashionshop.jpa.JpaUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductViewsService {

    @Autowired
    private JpaProductViews jpaProductViews;

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private JpaUser jpaUser;

    public void recordView(Integer productId, Integer userId) {
        ProductViewsEntity view = new ProductViewsEntity();
        view.setProduct(jpaProduct.findById(productId).orElse(null));
        view.setUser(jpaUser.findById(userId).orElse(null));
        view.setSearchTime(LocalDateTime.now());
        jpaProductViews.save(view);
    }

    public List<ProductViewDTO> getRecentViewDTOs(Integer userId) {
        UserEntity user = jpaUser.findById(userId).orElse(null);
        if (user == null) return List.of(); // Tránh null pointer

        List<ProductViewsEntity> views = jpaProductViews.findTop10ByUserOrderBySearchTimeDesc(user);
        return views.stream()
                    .map(this::convertToDTO)
                    .toList();
    }

    public List<ProductEntity> getTopViewedProducts() {
        return jpaProductViews.findTopViewedProducts();
    }
    
    public ProductViewDTO convertToDTO(ProductViewsEntity productView) {
        ProductViewDTO dto = new ProductViewDTO();
        dto.setId(productView.getId());
        dto.setSearchTime(productView.getSearchTime());

        // Chuyển product sang ProductDTO
        ProductEntity product = productView.getProduct();
        if (product != null) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(product.getProductId());
            productDTO.setName(product.getName());
            // Bổ sung thêm thuộc tính cần thiết nếu có
            dto.setProduct(List.of(productDTO));
        }

        // Chuyển user sang UserDTO
        UserEntity user = productView.getUser();
        if (user != null) {
            UserDTO userDTO = new UserDTO(null, null, null, null, false, null, null);
            userDTO.setId(user.getUserId());
            userDTO.setName(user.getFullName());
            userDTO.setEmail(user.getEmail());
            // Bổ sung thêm thuộc tính nếu cần
            dto.setUser(List.of(userDTO));
        }

        return dto;
    }

}
