package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.ColorsEntity;

public interface JpaColors extends JpaRepository<ColorsEntity, Integer> {
	boolean existsByColorNameIgnoreCase(String colorName);

	@Query("SELECT c FROM ColorsEntity c ORDER BY c.colorId ASC")
	Page<ColorsEntity> findAll(Pageable pageable);

	
	Page<ColorsEntity> findByColorNameContainingIgnoreCase(String keyword, Pageable pageable);


}
