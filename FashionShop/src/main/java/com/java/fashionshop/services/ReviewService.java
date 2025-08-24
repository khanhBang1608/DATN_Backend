package com.java.fashionshop.services;

import com.java.fashionshop.dto.ReviewDTO;
import com.java.fashionshop.dto.ReviewMediaDTO;
import com.java.fashionshop.entity.OrderDetailEntity;
import com.java.fashionshop.entity.ReviewEntity;
import com.java.fashionshop.entity.ReviewMediaEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaOrderDetail;
import com.java.fashionshop.jpa.JpaReview;
import com.java.fashionshop.jpa.JpaReviewMedia;
import com.java.fashionshop.jpa.JpaUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
public class ReviewService {

    @Autowired
    private JpaReview reviewRepository;
    @Autowired
    private JpaReviewMedia reviewMediaRepository;
    @Autowired
    private JpaUser userRepository;
    @Autowired
    private JpaOrderDetail orderDetailRepository;

    public List<ReviewDTO> getAllReviews(List<Integer> ratings, LocalDateTime startDate, LocalDateTime endDate, String userFullName) {
        List<ReviewEntity> reviews = reviewRepository.findReviewsWithFilters(ratings, startDate, endDate, userFullName);
        return reviews.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public boolean hasReviewForOrderDetail(Integer orderDetailId) {
        return reviewRepository.existsByOrderDetail_OrderDetailId(orderDetailId);
    }

    // Thêm phương thức kiểm tra trạng thái đánh giá cho nhiều orderDetailId
    public Map<Integer, Boolean> checkReviewsForOrderDetails(List<Integer> orderDetailIds) {
        List<ReviewEntity> reviews = reviewRepository.findByOrderDetail_OrderDetailIdIn(orderDetailIds);
        Map<Integer, Boolean> result = orderDetailIds.stream()
                .collect(Collectors.toMap(
                        id -> id,
                        id -> reviews.stream().anyMatch(review -> review.getOrderDetail().getOrderDetailId().equals(id))
                ));
        return result;
    }

    @Transactional
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        UserEntity user = getCurrentUser();

        OrderDetailEntity orderDetail = orderDetailRepository.findById(reviewDTO.getOrderDetailId())
                .orElseThrow(() -> new RuntimeException("Order detail not found with ID: " + reviewDTO.getOrderDetailId()));

        if (orderDetail.getReview() != null) {
            throw new RuntimeException("This order detail has already been reviewed.");
        }

        ReviewEntity review = new ReviewEntity();
        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());
        review.setReviewDate(LocalDateTime.now());
        review.setUser(user);
        review.setOrderDetail(orderDetail);

        // Media
        if (reviewDTO.getMedia() != null) {
            List<ReviewMediaEntity> mediaEntities = reviewDTO.getMedia().stream()
                    .map(mediaDTO -> {
                        ReviewMediaEntity media = new ReviewMediaEntity();
                        media.setReviewUrl(mediaDTO.getReviewUrl());
                        media.setReviewType(mediaDTO.getReviewType());
                        media.setReview(review);
                        return media;
                    }).collect(Collectors.toList());
            review.setMedia(mediaEntities);
        }

        ReviewEntity savedReview = reviewRepository.save(review);
        return convertToDTO(savedReview);
    }

    public List<ReviewDTO> getReviewsByProductId(Integer productId) {
        return reviewRepository.findByOrderDetail_ProductVariant_Product_ProductId(productId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Integer> getOrderDetailIdForCurrentUserAndProduct(Integer productId) {
        UserEntity user = getCurrentUser();
        return orderDetailRepository
                .findUnreviewedOrderDetailByProductIdAndUserId(productId, user.getUserId())
                .map(OrderDetailEntity::getOrderDetailId);
    }

    public List<ReviewDTO> getReviewsByCurrentUser() {
        UserEntity user = getCurrentUser();
        return reviewRepository.findByUser_UserId(user.getUserId()).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReviewDTO updateReview(Integer reviewId, ReviewDTO reviewDTO) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        UserEntity currentUser = getCurrentUser();
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: Cannot update another user's review");
        }

        review.setRating(reviewDTO.getRating());
        review.setComment(reviewDTO.getComment());

        review.getMedia().clear();
        if (reviewDTO.getMedia() != null) {
            List<ReviewMediaEntity> mediaEntities = reviewDTO.getMedia().stream()
                    .map(mediaDTO -> {
                        ReviewMediaEntity media = new ReviewMediaEntity();
                        media.setReviewUrl(mediaDTO.getReviewUrl());
                        media.setReviewType(mediaDTO.getReviewType());
                        media.setReview(review);
                        return media;
                    }).collect(Collectors.toList());
            review.setMedia(mediaEntities);
        }

        ReviewEntity updatedReview = reviewRepository.save(review);
        return convertToDTO(updatedReview);
    }

    public ReviewDTO getReviewById(Integer reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));
        return convertToDTO(review);
    }

    @Transactional
    public void deleteReview(Integer reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        UserEntity currentUser = getCurrentUser();
        if (!review.getUser().getUserId().equals(currentUser.getUserId())) {
            throw new RuntimeException("Unauthorized: Cannot delete another user's review");
        }

        reviewRepository.delete(review);
    }
    
    @Transactional
    public void deleteReviewByAdmin(Integer reviewId) {
        ReviewEntity review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found with ID: " + reviewId));

        // Admin có quyền xóa review mà không cần check chủ sở hữu
        reviewRepository.delete(review);
    }


    private ReviewDTO convertToDTO(ReviewEntity review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setReviewId(review.getReviewId());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setReviewDate(review.getReviewDate());
        dto.setUserId(review.getUser().getUserId());
        dto.setUserFullName(review.getUser().getFullName());
        dto.setOrderDetailId(review.getOrderDetail().getOrderDetailId());
        dto.setProductName(review.getOrderDetail().getProductVariant().getProduct().getName());
        dto.setProductId(review.getOrderDetail().getProductVariant().getProduct().getProductId());

        if (review.getMedia() != null) {
            dto.setMedia(review.getMedia().stream().map(media -> {
                ReviewMediaDTO mediaDTO = new ReviewMediaDTO();
                mediaDTO.setMediaId(media.getMediaId());
                mediaDTO.setReviewUrl(media.getReviewUrl());
                mediaDTO.setReviewType(media.getReviewType());
                return mediaDTO;
            }).collect(Collectors.toList()));
        }

        return dto;
    }

    private UserEntity getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found for email: " + email));
    }

    public Double getAverageRatingByProductId(Integer productId) {
        List<ReviewEntity> reviews = reviewRepository.findByOrderDetail_ProductVariant_Product_ProductId(productId);
        if (reviews.isEmpty()) return null;

        return reviews.stream()
                .mapToInt(ReviewEntity::getRating)
                .average()
                .orElse(0.0);
    }

}