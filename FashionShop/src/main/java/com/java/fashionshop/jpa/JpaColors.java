package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.ColorsEntity;

public interface JpaColors extends JpaRepository<ColorsEntity, Integer> {
	boolean existsByColorNameIgnoreCase(String colorName);

	Page<ColorsEntity> findAll(Pageable pageable);
	
	Page<ColorsEntity> findByColorNameContainingIgnoreCase(String keyword, Pageable pageable);


}
