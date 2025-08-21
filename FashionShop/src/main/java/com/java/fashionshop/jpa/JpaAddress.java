package com.java.fashionshop.jpa;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.AddressEntity;

public interface JpaAddress extends JpaRepository<AddressEntity, Integer> {
	List<AddressEntity> findByUserUserId(Integer userId);

	List<AddressEntity> findByUser_UserId(Integer userUserId);


	List<AddressEntity> findByAddressId(Integer addressId);
}
