package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.ReviewMediaEntity;

public interface JpaReviewMedia extends JpaRepository<ReviewMediaEntity, Integer> {

}
