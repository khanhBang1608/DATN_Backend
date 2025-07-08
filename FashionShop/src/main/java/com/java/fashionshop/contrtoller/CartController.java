package com.java.fashionshop.contrtoller;

import com.java.fashionshop.dto.CartDTO;
//import com.java.fashionshop.request.AddItemCartRequest;
import com.java.fashionshop.request.AddItemRequest;
import com.java.fashionshop.request.UpdateItemCartRequest;
import com.java.fashionshop.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// Controller
@RestController
@RequestMapping("/api/cart")
class CartController {
    @Autowired
    private CartService cartService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartDTO> getCart() {
        return ResponseEntity.ok(cartService.getCartByUserId());
    }

    @PostMapping(value = "/add", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartDTO> addItemToCart(@RequestBody AddItemRequest request) {
        return ResponseEntity.ok(cartService.addItemToCart(
                request.getProductVariantId(),
                request.getQuantity()
        ));
    }

    @PutMapping(value = "/update/{cartDetailId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartDTO> updateCartItem(
            @PathVariable Integer cartDetailId,
            @RequestBody UpdateItemCartRequest request) {
        return ResponseEntity.ok(cartService.updateCartItem(
                cartDetailId,
                request.getQuantity()
        ));
    }

    @DeleteMapping(value = "/remove/{cartDetailId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartDTO> removeCartItem(@PathVariable Integer cartDetailId) {
        return ResponseEntity.ok(cartService.removeCartItem(cartDetailId));
    }

    @DeleteMapping(value = "/clear", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CartDTO> clearCart() {
        return ResponseEntity.ok(cartService.clearCart());
    }
}