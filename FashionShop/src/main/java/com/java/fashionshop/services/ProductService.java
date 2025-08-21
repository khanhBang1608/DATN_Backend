package com.java.fashionshop.services;

import com.java.fashionshop.bean.ProductBean;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.dto.SystemProductStatsDTO;
import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaCartDetail;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaFavorite;
import com.java.fashionshop.jpa.JpaProduct;
import com.java.fashionshop.jpa.JpaProductViews;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

	@Autowired
	private JpaProduct jpaProduct;

	@Autowired
	private JpaCategory jpaCategory;
	@Autowired
	private JpaFavorite jpaFavorite;
	@Autowired
	private JpaProductViews jpaProductViews;
	@Autowired
	private JpaCartDetail jpaCartDetail;

	// Lấy tất cả sản phẩm
	public List<ProductBean> findAll() {
		List<ProductEntity> products = jpaProduct.findAll();
		return products.stream().map(this::convertToBean).collect(Collectors.toList());
	}

	public Page<ProductDTO> getPaginatedProducts(Pageable pageable) {
		Page<ProductEntity> productPage = jpaProduct.findAll(pageable);

		// Chuyển đổi entity -> DTO, và chỉ giữ lại các sản phẩm có variant
		List<ProductDTO> filteredDTOs = productPage.stream().map(this::convertToDTO)
				.filter(dto -> dto.getVariants() != null && !dto.getVariants().isEmpty())
				.filter(dto -> Boolean.TRUE.equals(dto.getStatus()) && Boolean.TRUE.equals(dto.getCategoryStatus()))
				.toList();

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
			dto.setCategoryStatus(product.getCategory().isStatus());
		}

		// 1️⃣ Lấy số lượt yêu thích
		Long favoriteCount = jpaFavorite.countByProductId(product.getProductId());
		dto.setFavoriteCount(favoriteCount != null ? favoriteCount.intValue() : 0);

		// 2️⃣ Lấy số lượt xem
		dto.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0);

		// 3️⃣ Lấy số lượt trong giỏ hàng
		Integer cartCount = jpaCartDetail.countByProductId(product.getProductId());
		dto.setCartCount(cartCount != null ? cartCount : 0);

		// 4️⃣ Danh sách biến thể
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
			dto.setVariants(List.of());
		}

		return dto;
	}

	public Page<ProductEntity> findTopNewestProductsWithVariants(Pageable pageable) {
		return jpaProduct.findAllByStatusTrueOrderByDateCreatedDesc(pageable);
	}
	public List<ProductDTO> findTop8NewestProductsWithVariants() {
	    return jpaProduct.findTop8ByStatusTrueOrderByDateCreatedDesc()
	            .stream()
	            .map(this::convertToDTO)
	            .filter(dto -> dto.getVariants() != null && !dto.getVariants().isEmpty())
	            .filter(dto -> Boolean.TRUE.equals(dto.getStatus()) && Boolean.TRUE.equals(dto.getCategoryStatus()))
	            .toList();
	}
	
	// Tìm sản phẩm theo danh mục
	public List<ProductEntity> findByCategoryId(Integer categoryId) {
		return jpaProduct.findByCategory_CategoryId(categoryId);
	}

	public Page<ProductDTO> searchProductsByNamePaged(String keyword, Pageable pageable) {
		Page<ProductEntity> entities = jpaProduct.searchByNameOrCategoryPaged(keyword, pageable);

		List<ProductDTO> filtered = entities.stream().filter(p -> p.getVariants() != null && !p.getVariants().isEmpty())
				.map(this::convertToDTO).toList();

		return new PageImpl<>(filtered, pageable, entities.getTotalElements());
	}

	public Page<ProductDTO> getRelatedProducts(Integer categoryId, Integer excludeProductId, Pageable pageable) {
		List<ProductEntity> relatedProducts = findByCategoryId(categoryId);

		List<ProductDTO> filtered = relatedProducts.stream()
				.filter(p -> excludeProductId == null || !p.getProductId().equals(excludeProductId))
				.filter(p -> p.getVariants() != null && !p.getVariants().isEmpty()).map(this::convertToDTO).toList();

		// Tính chỉ mục bắt đầu và kết thúc theo page
		int start = (int) pageable.getOffset();
		int end = Math.min(start + pageable.getPageSize(), filtered.size());

		List<ProductDTO> paginatedList = (start <= end) ? filtered.subList(start, end) : List.of();

		return new PageImpl<>(paginatedList, pageable, filtered.size());
	}

	public SystemProductStatsDTO getSystemProductStats() {
		List<ProductEntity> products = jpaProduct.findAll();

		int totalVariants = 0;
		long totalStock = 0;

		for (ProductEntity product : products) {
			if (product.getVariants() != null) {
				totalVariants += product.getVariants().size();
				totalStock += product.getVariants().stream().mapToLong(v -> v.getStock() != null ? v.getStock() : 0)
						.sum();
			}
		}

		SystemProductStatsDTO stats = new SystemProductStatsDTO();
		stats.setTotalVariants(totalVariants);
		stats.setTotalStock(totalStock);

		return stats;
	}

}