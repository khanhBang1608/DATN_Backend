package com.java.fashionshop.controller;

import com.java.fashionshop.dto.ReviewDTO;
import com.java.fashionshop.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reviews")
public class ManageReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReviewDTO>> getAllReviews(
            @RequestParam(required = false) List<Integer> ratings,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String userFullName,
            @RequestParam(required = false) Boolean isHidden) {
        List<ReviewDTO> reviews = reviewService.getAllReviews(ratings, startDate, endDate, userFullName, isHidden);
        return ResponseEntity.ok(reviews);
    }

    @GetMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Integer reviewId) {
        ReviewDTO review = reviewService.getReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/{reviewId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> hideReview(@PathVariable Integer reviewId, @RequestParam boolean hide) {
        reviewService.hideReview(reviewId, hide);
        return ResponseEntity.noContent().build();
    }
}