package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ReviewEntity;

public interface JpaReview extends JpaRepository<ReviewEntity, Integer> {

}
