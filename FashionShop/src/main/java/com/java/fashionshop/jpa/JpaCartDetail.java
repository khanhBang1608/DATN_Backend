package com.java.fashionshop.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.java.fashionshop.entity.CartDetailEntity;

import java.util.Optional;

public interface JpaCartDetail extends JpaRepository<CartDetailEntity, Integer> {
    Optional<CartDetailEntity> findByCartCartIdAndProductVariantProductVariantId(Integer cartId, Integer productVariantId);
    
    @Query("SELECT SUM(cd.quantity) FROM CartDetailEntity cd " +
    	       "WHERE cd.productVariant.product.productId = :productId")
    	Integer countByProductId(@Param("productId") Integer productId);

}
