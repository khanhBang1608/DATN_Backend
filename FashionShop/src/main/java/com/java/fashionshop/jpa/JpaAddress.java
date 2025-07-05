package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.AddressEntity;

public interface JpaAddress extends JpaRepository<AddressEntity, Integer> {

}
