package com.java.fashionshop.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.java.fashionshop.dto.CartDTO;
import com.java.fashionshop.dto.CartDetailDTO;
import com.java.fashionshop.entity.CartDetailEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.jpa.JpaCartDetail;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.jpa.JpaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.java.fashionshop.entity.CartEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaCart;
@Service
public class CartService {
    private static final Logger logger = LoggerFactory.getLogger(CartService.class);

    @Autowired
    private JpaCart cartRepository;
    @Autowired
    private JpaCartDetail cartDetailRepository;
    @Autowired
    private JpaProductVariant productVariantRepository;
    @Autowired
    private JpaUser userRepository;

    public CartEntity save(CartEntity cart) {
        return cartRepository.save(cart);
    }

    private Integer getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername(); // Email from JWT
        } else {
            email = principal.toString();
        }
        logger.debug("Fetching user by email: {}", email);
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("User not found for email: {}", email);
                    return new RuntimeException("User not found for email: " + email);
                });
        return user.getUserId();
    }

    public CartDTO getCartByUserId() {
        Integer userId = getAuthenticatedUserId();
        logger.debug("Attempting to fetch cart for userId: {}", userId);
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for userId: {}", userId);
                    return new RuntimeException("User not found for userId: " + userId);
                });
        CartEntity cart = cartRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    logger.info("Creating new cart for userId: {}", userId);
                    return createCartForUser(user);
                });

        List<CartDetailDTO> detailDTOs = cart.getDetails().stream()
                .map(detail -> new CartDetailDTO(
                        detail.getCartDetailId(),
                        detail.getProductVariant().getProductVariantId(),
                        detail.getQuantity(),
                        detail.getProductVariant().getPrice(),
                        detail.getProductVariant().getProduct().getName(),
                        detail.getProductVariant().getImageName(),
                        detail.getProductVariant().getSize().getSizeName(),
                        detail.getProductVariant().getColor().getColorName(),
                        detail.getProductVariant().getStock()

                ))
                .collect(Collectors.toList());

        logger.debug("Returning cart for userId: {} with {} items", userId, detailDTOs.size());
        return new CartDTO(cart.getCartId(), userId, detailDTOs);
    }

    public CartDTO addItemToCart(Integer productVariantId, Integer quantity) {
    Integer userId = getAuthenticatedUserId();

    if (quantity <= 0) {
        logger.error("Invalid quantity: {}", quantity);
        throw new IllegalArgumentException("Quantity must be greater than 0");
    }

    UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> {
                logger.error("User not found for userId: {}", userId);
                return new RuntimeException("User not found for userId: " + userId);
            });

    ProductVariantEntity productVariant = productVariantRepository.findById(productVariantId)
            .orElseThrow(() -> {
                logger.error("Product variant not found for productVariantId: {}", productVariantId);
                return new RuntimeException("Product variant not found");
            });

    CartEntity cart = cartRepository.findByUserUserId(userId)
            .orElseGet(() -> {
                logger.info("Creating new cart for userId: {}", userId);
                return createCartForUser(user);
            });

    Optional<CartDetailEntity> existingDetail = cartDetailRepository
            .findByCartCartIdAndProductVariantProductVariantId(cart.getCartId(), productVariantId);

    // ✅ Tính tổng số lượng sau khi cộng dồn
    int totalQuantity = quantity;
    if (existingDetail.isPresent()) {
        totalQuantity += existingDetail.get().getQuantity();
    }

    // ✅ Kiểm tra tồn kho
    if (productVariant.getStock() < totalQuantity) {
        logger.error("Insufficient stock for productVariantId: {}, requested total: {}, available: {}",
                productVariantId, totalQuantity, productVariant.getStock());
        throw new RuntimeException("Số lượng vượt quá tồn kho. Hiện chỉ còn " + productVariant.getStock());
    }

    CartDetailEntity detail;
    if (existingDetail.isPresent()) {
        detail = existingDetail.get();
        detail.setQuantity(detail.getQuantity() + quantity);
        logger.debug("Updated quantity for cartDetailId: {} to {}", detail.getCartDetailId(), detail.getQuantity());
    } else {
        detail = new CartDetailEntity();
        detail.setCart(cart);
        detail.setProductVariant(productVariant);
        detail.setQuantity(quantity);
        cart.getDetails().add(detail);
        logger.debug("Added new item to cart for productVariantId: {}", productVariantId);
    }

    cartDetailRepository.save(detail);
    cartRepository.save(cart);

    return getCartByUserId();
}

    public CartDTO updateCartItem(Integer cartDetailId, Integer quantity) {
        Integer userId = getAuthenticatedUserId();
        if (quantity <= 0) {
            logger.error("Invalid quantity: {}", quantity);
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        CartDetailEntity detail = cartDetailRepository.findById(cartDetailId)
                .orElseThrow(() -> {
                    logger.error("Cart detail not found for cartDetailId: {}", cartDetailId);
                    return new RuntimeException("Cart detail not found");
                });

        if (!detail.getCart().getUser().getUserId().equals(userId)) {
            logger.error("Unauthorized access to cartDetailId: {} by userId: {}", cartDetailId, userId);
            throw new RuntimeException("Unauthorized access to cart");
        }

        ProductVariantEntity productVariant = detail.getProductVariant();
        if (productVariant.getStock() < quantity) {
            logger.error("Insufficient stock for productVariantId: {}, requested: {}, available: {}",
                    productVariant.getProductVariantId(), quantity, productVariant.getStock());
            throw new RuntimeException("Insufficient stock");
        }

        detail.setQuantity(quantity);
        cartDetailRepository.save(detail);
        logger.debug("Updated quantity for cartDetailId: {} to {}", cartDetailId, quantity);

        return getCartByUserId();
    }

    public CartDTO removeCartItem(Integer cartDetailId) {
        Integer userId = getAuthenticatedUserId();
        CartDetailEntity detail = cartDetailRepository.findById(cartDetailId)
                .orElseThrow(() -> {
                    logger.error("Cart detail not found for cartDetailId: {}", cartDetailId);
                    return new RuntimeException("Cart detail not found");
                });

        if (!detail.getCart().getUser().getUserId().equals(userId)) {
            logger.error("Unauthorized access to cartDetailId: {} by userId: {}", cartDetailId, userId);
            throw new RuntimeException("Unauthorized access to cart");
        }

        cartDetailRepository.delete(detail);
        logger.debug("Removed cartDetailId: {}", cartDetailId);
        return getCartByUserId();
    }

    public CartDTO clearCart() {
        Integer userId = getAuthenticatedUserId();
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    logger.error("User not found for userId: {}", userId);
                    return new RuntimeException("User not found for userId: " + userId);
                });
        CartEntity cart = cartRepository.findByUserUserId(userId)
                .orElseGet(() -> {
                    logger.info("Creating new cart for userId: {}", userId);
                    return createCartForUser(user);
                });

        cart.getDetails().clear();
        cartRepository.save(cart);
        logger.debug("Cleared cart for userId: {}", userId);

        return getCartByUserId();
    }

    private CartEntity createCartForUser(UserEntity user) {
        CartEntity cart = new CartEntity();
        cart.setUser(user);
        cart.setDetails(new ArrayList<>());
        return cartRepository.save(cart);
    }
}


