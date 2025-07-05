package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

import com.java.fashionshop.entity.FavoriteEntity;

public interface JpaFavorite extends JpaRepository<FavoriteEntity, Integer> {

}
