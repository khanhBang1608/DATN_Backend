package com.java.fashionshop.services;

import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ProductViewsService {

	@Autowired
	private JpaProductViews jpaProductViews;

	@Autowired
	private JpaProduct jpaProduct;

	@Autowired
	private JpaUser jpaUser;

	// Ghi nhận lượt xem
	public void recordView(Integer productId, Integer userId) {
		ProductViewsEntity view = new ProductViewsEntity();
		ProductEntity product = jpaProduct.findById(productId).orElse(null);
		if (product != null) {
			view.setProduct(product);
			view.setUser(jpaUser.findById(userId).orElse(null));
			view.setSearchTime(LocalDateTime.now());
			jpaProductViews.save(view);

			// Tăng viewCount trong ProductEntity
			product.setViewCount(product.getViewCount() != null ? product.getViewCount() + 1 : 1);
			jpaProduct.save(product);
		}
	}

	public Page<ProductViewDTO> getRecentViewDTOsPaged(Integer userId, Pageable pageable) {
	    UserEntity user = jpaUser.findById(userId).orElse(null);
	    if (user == null) return Page.empty();

	    Page<ProductViewsEntity> pagedViews = jpaProductViews.findByUserOrderBySearchTimeDesc(user, Pageable.unpaged());

	    // Lọc trùng theo productId, ưu tiên search_time mới nhất
	    Map<Integer, ProductViewsEntity> uniqueProductMap = new LinkedHashMap<>();
	    for (ProductViewsEntity view : pagedViews.getContent()) {
	        Integer productId = view.getProduct().getProductId();
	        if (!uniqueProductMap.containsKey(productId)) {
	            uniqueProductMap.put(productId, view); // giữ lại lần đầu tiên xuất hiện (là mới nhất do đã sort)
	        }
	    }

	    List<ProductViewDTO> filteredList = uniqueProductMap.values().stream()
	            .map(this::convertToDTO)
	            .collect(Collectors.toList());

	    // Tính phân trang thủ công sau khi đã lọc
	    int start = (int) pageable.getOffset();
	    int end = Math.min(start + pageable.getPageSize(), filteredList.size());
	    List<ProductViewDTO> pagedList = filteredList.subList(start, end);

	    return new PageImpl<>(pagedList, pageable, filteredList.size());
	}


	// Chuyển đổi ProductViewsEntity sang ProductViewDTO
	public ProductViewDTO convertToDTO(ProductViewsEntity productView) {
		ProductViewDTO dto = new ProductViewDTO();
		dto.setId(productView.getId());
		dto.setSearchTime(productView.getSearchTime());

		// Chuyển đổi ProductEntity -> ProductDTO
		ProductEntity product = productView.getProduct();
		if (product != null) {
			ProductDTO productDTO = new ProductDTO();
			productDTO.setProductId(product.getProductId());
			productDTO.setName(product.getName());
			productDTO.setDescription(product.getDescription());
			productDTO.setDateCreated(product.getDateCreated());
			productDTO.setStatus(product.getStatus());
			productDTO.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0); // Lấy viewCount từ
																									// ProductEntity

			if (product.getCategory() != null) {
				productDTO.setCategoryId(product.getCategory().getCategoryId());
				productDTO.setCategoryName(product.getCategory().getCategoryName());
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
				}).collect(Collectors.toList());

				productDTO.setVariants(variantDTOs);
			}

			dto.setProduct(List.of(productDTO));
		}

		// Chuyển đổi UserEntity -> UserDTO
		UserEntity user = productView.getUser();
		if (user != null) {
			UserDTO userDTO = new UserDTO(null, null, null, null, false, null, null);
			userDTO.setId(user.getUserId());
			userDTO.setName(user.getFullName());
			userDTO.setEmail(user.getEmail());
			userDTO.setAvatar(user.getAvatar());
			userDTO.setStatus(user.getStatus() != null && user.getStatus());
			userDTO.setRole(user.getRole());
			userDTO.setCreatedAt(user.getDateCreated());

			dto.setUser(List.of(userDTO));
		}

		return dto;
	}
}