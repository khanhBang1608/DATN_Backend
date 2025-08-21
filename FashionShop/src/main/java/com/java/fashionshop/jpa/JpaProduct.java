 package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import com.java.fashionshop.entity.ProductEntity;

public interface JpaProduct extends JpaRepository<ProductEntity, Integer> {
	boolean existsById(Integer id);
	@Query("SELECT DISTINCT p FROM ProductEntity p JOIN FETCH p.variants v WHERE p.status = true ORDER BY p.dateCreated DESC")
	List<ProductEntity> findTop10WithVariants(Pageable pageable);
	@Query("SELECT p.productId, SUM(v.stock) FROM ProductEntity p JOIN p.variants v GROUP BY p.productId")
	List<Object[]> findTotalStockPerProduct();
	List<ProductEntity> findByCategory_CategoryId(Integer categoryId);
	@Query("SELECT SUM(v.stock) FROM ProductEntity p JOIN p.variants v WHERE p.productId = :productId")
	Long getTotalStockByProductId(@Param("productId") Integer productId);

	@Query(
		    value = "SELECT p.* FROM product p " +
		            "JOIN category c ON p.category_id = c.category_id " +
		            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		            "   OR LOWER(c.category_name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
		    countQuery = "SELECT COUNT(*) FROM product p " +
		            "JOIN category c ON p.category_id = c.category_id " +
		            "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
		            "   OR LOWER(c.category_name) LIKE LOWER(CONCAT('%', :keyword, '%'))",
		    nativeQuery = true
		)
		Page<ProductEntity> searchByNameOrCategoryPaged(@Param("keyword") String keyword, Pageable pageable);

	Page<ProductEntity> findAllByStatusTrueOrderByDateCreatedDesc(Pageable pageable);
	Page<ProductEntity> findAll(Pageable pageable);
	@Query("SELECT p.name FROM ProductEntity p " +
		       "JOIN p.variants v " +
		       "WHERE v.productVariantId = :variantId")
		String findProductNameByVariantId(@Param("variantId") Integer variantId);
	List<ProductEntity> findTop8ByStatusTrueOrderByDateCreatedDesc();

}
