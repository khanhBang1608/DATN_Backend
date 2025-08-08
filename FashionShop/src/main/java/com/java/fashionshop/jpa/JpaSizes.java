package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.SizesEntity;

public interface JpaSizes extends JpaRepository<SizesEntity, Integer> {
	boolean existsBySizeNameIgnoreCase(String sizeName);

	Page<SizesEntity> findAll(Pageable pageable);
	
	Page<SizesEntity> findBySizeNameContainingIgnoreCase(String keyword, Pageable pageable);

}
