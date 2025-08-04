package com.java.fashionshop.services;

import com.java.fashionshop.bean.ProductBean;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaProduct;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

	@Autowired
	private JpaProduct jpaProduct;

	@Autowired
	private JpaCategory jpaCategory;

	// Lấy tất cả sản phẩm
	public List<ProductBean> findAll() {
		List<ProductEntity> products = jpaProduct.findAll();
		return products.stream().map(this::convertToBean).collect(Collectors.toList());
	}

	public Page<ProductDTO> getPaginatedProducts(Pageable pageable) {
		Page<ProductEntity> productPage = jpaProduct.findAll(pageable);

		// Chuyển đổi entity -> DTO, và chỉ giữ lại các sản phẩm có variant
		List<ProductDTO> filteredDTOs = productPage.stream().map(this::convertToDTO)
				.filter(dto -> dto.getVariants() != null && !dto.getVariants().isEmpty()).toList();

		// Trả về Page thủ công (nếu đã filter mất phần tử)
		return new PageImpl<>(filteredDTOs, pageable, productPage.getTotalElements());
	}

	// Tìm sản phẩm theo ID
	public ProductBean findById(Integer id) {
		return jpaProduct.findById(id).map(this::convertToBean).orElse(null);
	}

	// Lưu hoặc cập nhật sản phẩm
	@Transactional
	public ProductEntity save(ProductBean bean) {
		if (bean.getName() == null || bean.getName().trim().isEmpty()) {
			throw new IllegalArgumentException("Tên sản phẩm trống");
		}

		if (bean.getDescription() == null || bean.getDescription().trim().isEmpty()) {
			throw new IllegalArgumentException("Mô tả sản phẩm trống");
		}

		if (bean.getCategoryId() == null) {
			throw new IllegalArgumentException("Chưa chọn danh mục");
		}

		// Kiểm tra trùng tên (trừ trường hợp cập nhật chính nó)
		boolean isDuplicate = jpaProduct.findAll().stream().anyMatch(p -> p.getName().equalsIgnoreCase(bean.getName())
				&& (bean.getProductId() == null || !p.getProductId().equals(bean.getProductId())));

		if (isDuplicate) {
			throw new IllegalArgumentException("Tên sản phẩm đã tồn tại");
		}

		ProductEntity entity;
		boolean isNew = (bean.getProductId() == null);

		if (isNew) {
			entity = new ProductEntity();
			entity.setDateCreated(LocalDateTime.now());
			entity.setViewCount(0); // Khởi tạo viewCount
		} else {
			entity = jpaProduct.findById(bean.getProductId())
					.orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
		}

		entity.setName(bean.getName().trim());
		entity.setDescription(bean.getDescription().trim());
		entity.setStatus(bean.getStatus() != null ? bean.getStatus() : true);

		CategoryEntity category = jpaCategory.findById(bean.getCategoryId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
		entity.setCategory(category);

		return jpaProduct.save(entity);
	}

	// Lấy tất cả sản phẩm dưới dạng entity
	public List<ProductEntity> getAllEntity() {
		return jpaProduct.findAll();
	}

	// Tìm sản phẩm theo ID dưới dạng entity
	public ProductEntity findEntityById(Integer id) {
		return jpaProduct.findById(id).orElse(null);
	}

	// Chuyển đổi ProductEntity sang ProductBean
	private ProductBean convertToBean(ProductEntity entity) {
		ProductBean bean = new ProductBean();
		bean.setProductId(entity.getProductId());
		bean.setName(entity.getName());
		bean.setDescription(entity.getDescription());
		bean.setStatus(entity.getStatus());
		bean.setCategoryId(entity.getCategory().getCategoryId());
		return bean;
	}

	// Chuyển đổi ProductEntity sang ProductDTO
	public ProductDTO convertToDTO(ProductEntity product) {
		ProductDTO dto = new ProductDTO();
		dto.setProductId(product.getProductId());
		dto.setName(product.getName());
		dto.setDescription(product.getDescription());
		dto.setStatus(product.getStatus());
		dto.setDateCreated(product.getDateCreated());
		dto.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0); // Thêm viewCount

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
			}).collect(Collectors.toList());

			dto.setVariants(variantDTOs);
		} else {
			dto.setVariants(new ArrayList<>());
		}

		return dto;
	}

	// Lấy top 10 sản phẩm mới nhất
	public List<ProductDTO> getTop10NewestProductsWithVariants() {
		Pageable pageable = PageRequest.of(0, 10);
		List<ProductEntity> products = jpaProduct.findTop10ByOrderByDateCreatedDesc(pageable);
		return products.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	// Tìm sản phẩm theo danh mục
	public List<ProductEntity> findByCategoryId(Integer categoryId) {
		return jpaProduct.findByCategory_CategoryId(categoryId);
	}

	// Tìm kiếm sản phẩm theo tên
	public List<ProductDTO> searchProductsByName(String keyword) {
		List<ProductEntity> entities = jpaProduct.searchByName(keyword);
		return entities.stream().filter(p -> p.getVariants() != null && !p.getVariants().isEmpty())
				.map(this::convertToDTO).collect(Collectors.toList());
	}

}