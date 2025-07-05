package com.java.fashionshop.jpa;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.UserEntity;

public interface JpaUser extends JpaRepository<UserEntity, Integer> {
	Optional<UserEntity> findByEmail(String email);
}
