package com.java.fashionshop.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.entity.CartEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaCart;

@Service
public class CartService {

    @Autowired
    private JpaCart cartJPA;

    public Optional<CartEntity> findById(Integer id) {
        return cartJPA.findById(id);
    }

    public CartEntity save(CartEntity cart) {
        return cartJPA.save(cart);
    }

    public void deleteById(Integer id) {
        cartJPA.deleteById(id);
    }

    public CartEntity getCartByUser(UserEntity user) {
        for (CartEntity cart : cartJPA.findAll()) {
            if (cart.getUser().getUserId().equals(user.getUserId())) {
                return cart;
            }
        }
        return null;
    }
}
