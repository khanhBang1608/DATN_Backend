package com.java.fashionshop.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import java.util.HashMap;

import com.java.fashionshop.bean.ProductPromotionBean;
import com.java.fashionshop.dto.ProductPromotionDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.ProductPromotionEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.PromotionsEntity;
import com.java.fashionshop.jpa.JpaProductPromotion;
import com.java.fashionshop.jpa.JpaPromotions;
import com.java.fashionshop.jpa.JpaProductVariant;

@Service
public class ProductPromotionService {

	@Autowired
	private JpaProductPromotion productPromotionRepo;

	@Autowired
	private JpaPromotions JpaPromotion;

	@Autowired
	private JpaProductVariant JpaProductVariant;

	public List<ProductVariantDTO> getVariantsByProductId(Integer productId) {
		List<ProductVariantEntity> variants = JpaProductVariant.findByProduct_ProductId(productId);

		return variants.stream().map(variant -> {
			ProductVariantDTO dto = new ProductVariantDTO();
			dto.setProductVariantId(variant.getProductVariantId());
			dto.setStock(variant.getStock());
			dto.setPrice(variant.getPrice());
			dto.setImageName(variant.getImageName());

			if (variant.getColor() != null) {
				dto.setColorId(variant.getColor().getColorId());
				dto.setColorName(variant.getColor().getColorName());
			}

			if (variant.getSize() != null) {
				dto.setSizeId(variant.getSize().getSizeId());
				dto.setSizeName(variant.getSize().getSizeName());
			}

			return dto;
		}).collect(Collectors.toList());
	}

	public List<ProductPromotionDTO> findByPromotionId(Integer promotionId) {
		List<ProductPromotionEntity> entities = productPromotionRepo.findByPromotion_Id(promotionId);
		return entities.stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	public Map<String, Object> findByPromotionIdPaged(Integer promotionId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		Page<ProductPromotionEntity> entityPage = productPromotionRepo.findByPromotion_Id(promotionId, pageable);

		List<ProductPromotionDTO> dtoList = entityPage.getContent().stream().map(this::convertToDTO).toList();

		Map<String, Object> response = new HashMap<>();
		response.put("items", dtoList);
		response.put("currentPage", entityPage.getNumber());
		response.put("totalItems", entityPage.getTotalElements());
		response.put("totalPages", entityPage.getTotalPages());

		return response;
	}

	public ProductPromotionEntity findById(Integer id) {
		return productPromotionRepo.findById(id).orElse(null);
	}

	public List<ProductPromotionDTO> findAll() {
		return productPromotionRepo.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
	}

	public List<ProductPromotionDTO> saveBulk(Integer promotionId, List<ProductPromotionBean> beans) {
		List<ProductPromotionDTO> result = new ArrayList<>();
		List<Integer> conflictedVariantIds = new ArrayList<>();

		for (ProductPromotionBean bean : beans) {
			PromotionsEntity promotion = JpaPromotion.findById(bean.getPromotionId()).orElse(null);
			ProductVariantEntity variant = JpaProductVariant.findById(bean.getProductVariantId()).orElse(null);

			if (promotion == null || variant == null)
				continue;

			if (isOverlappingPromotion(bean.getProductVariantId(), promotionId, promotion.getStartDate(),
					promotion.getEndDate())) {
				conflictedVariantIds.add(bean.getProductVariantId());
				continue;
			}

			ProductPromotionEntity entity = new ProductPromotionEntity();
			entity.setPromotion(promotion);
			entity.setProductVariant(variant);
//        entity.setQuantityLimit(bean.getQuantityLimit());

			result.add(convertToDTO(productPromotionRepo.save(entity)));
		}
		return result;
	}

	public ProductPromotionDTO update(Integer id, ProductPromotionBean bean) {
		ProductPromotionEntity entity = productPromotionRepo.findById(id).orElse(null);
		if (entity == null)
			return null;

		PromotionsEntity promotion = JpaPromotion.findById(bean.getPromotionId()).orElse(null);
		ProductVariantEntity variant = JpaProductVariant.findById(bean.getProductVariantId()).orElse(null);

		if (promotion == null || variant == null)
			return null;
		// ⚠️ Thêm đoạn kiểm tra trùng thời gian ở đây
		if (isOverlappingPromotion(bean.getProductVariantId(), bean.getPromotionId(), promotion.getStartDate(),
				promotion.getEndDate())) {
			return null;
		}
//        entity.setQuantityLimit(bean.getQuantityLimit());
		entity.setPromotion(promotion);
		entity.setProductVariant(variant);

		return convertToDTO(productPromotionRepo.save(entity));
	}

	public void delete(Integer id) {
		productPromotionRepo.deleteById(id);
	}

	public ProductPromotionDTO convertToDTO(ProductPromotionEntity entity) {
		ProductPromotionDTO dto = new ProductPromotionDTO();
		dto.setId(entity.getId());
		dto.setProductVariantId(entity.getProductVariant().getProductVariantId());
		dto.setPromotionId(entity.getPromotion().getId());

		// Convert đầy đủ thông tin productVariant
		ProductVariantEntity variant = entity.getProductVariant();
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

		dto.setProductVariant(variantDTO); // Gán đầy đủ vào DTO cha

		Double originalPrice = variant.getPrice().doubleValue();
		Double discountPercent = entity.getPromotion().getDiscountAmount();
		Double discountedPrice = originalPrice * (1 - discountPercent / 100);
		dto.setDiscountedPrice(discountedPrice);
		return dto;
	}

	private boolean isOverlappingPromotion(Integer variantId, Integer promotionId, LocalDate newStart,
			LocalDate newEnd) {
		List<ProductPromotionEntity> existing = productPromotionRepo.findByProductVariant_ProductVariantId(variantId);

		for (ProductPromotionEntity item : existing) {
			PromotionsEntity promo = item.getPromotion();

			// Bỏ qua promotion đang xét (khi update)
			if (promo.getId().equals(promotionId))
				continue;

			// Kiểm tra trùng thời gian
			boolean isOverlap = !(promo.getEndDate().isBefore(newStart) || promo.getStartDate().isAfter(newEnd));
			if (isOverlap)
				return true;
		}

		return false;
	}

}
