package com.java.fashionshop.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.java.fashionshop.entity.UserEntity;

public interface JpaUser extends JpaRepository<UserEntity, Integer> {
	Optional<UserEntity> findByEmail(String email);
	long countByRole(Integer role);
	
	Page<UserEntity> findByRoleNot(int role, Pageable pageable);
}
