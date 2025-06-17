package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ColorsEntity;

public interface JpaColors extends JpaRepository<ColorsEntity, Integer> {

}
