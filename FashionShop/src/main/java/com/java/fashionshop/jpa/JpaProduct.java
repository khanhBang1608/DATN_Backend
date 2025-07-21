package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.ProductEntity;

public interface JpaProduct extends JpaRepository<ProductEntity, Integer> {
	boolean existsById(Integer id);
	@Query("SELECT DISTINCT p FROM ProductEntity p JOIN FETCH p.variants v WHERE p.status = true ORDER BY p.dateCreated DESC")
	List<ProductEntity> findTop10WithVariants(Pageable pageable);

}
