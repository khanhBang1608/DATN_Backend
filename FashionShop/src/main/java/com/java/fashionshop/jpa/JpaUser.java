package com.java.fashionshop.jpa;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.UserEntity;

public interface JpaUser extends JpaRepository<UserEntity, Integer> {
	Optional<UserEntity> findByEmail(String email);
	long countByRole(Integer role);
	
	Page<UserEntity> findByRoleNot(int role, Pageable pageable);
	
	@Query(value = """
		    SELECT 
		        FORMAT(date_created, 'MM/yyyy') AS monthYear,
		        COUNT(*) AS userCount
		    FROM [User]
		    GROUP BY FORMAT(date_created, 'MM/yyyy')
		    ORDER BY MIN(date_created) ASC
		    """, nativeQuery = true)
		List<Map<String, Object>> countUsersByMonthYear();

}
