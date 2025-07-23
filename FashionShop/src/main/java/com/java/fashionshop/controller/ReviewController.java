    package com.java.fashionshop.controller;

    import com.java.fashionshop.dto.ReviewDTO;
    import com.java.fashionshop.dto.ReviewMediaDTO;
    import com.java.fashionshop.entity.OrderDetailEntity;
    import com.java.fashionshop.entity.UserEntity;
    import com.java.fashionshop.jpa.JpaOrderDetail;
    import com.java.fashionshop.services.ReviewService;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.security.core.annotation.AuthenticationPrincipal;
    import org.springframework.security.core.userdetails.UserDetails;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Path;
    import java.nio.file.Paths;
    import java.util.HashMap;
    import java.util.List;
    import java.util.Map;
    import java.util.Optional;
    @RestController
    @RequestMapping("/api/user/reviews")
    public class ReviewController {

        @Autowired
        private ReviewService reviewService;


        @PostMapping(consumes = {"multipart/form-data"})
        public ResponseEntity<ReviewDTO> createReview(
                @RequestParam("rating") int rating,
                @RequestParam("comment") String comment,
                @RequestParam("orderDetailId") Integer orderDetailId,
                @RequestParam("reviewType") String reviewType,
                @RequestParam(value = "media", required = false) MultipartFile media) throws IOException {
            ReviewDTO reviewDTO = new ReviewDTO();
            reviewDTO.setRating(rating);
            reviewDTO.setComment(comment);
            reviewDTO.setOrderDetailId(orderDetailId);

            if (media != null && !media.isEmpty() && !"text".equals(reviewType)) {
                String fileName = System.currentTimeMillis() + "_" + media.getOriginalFilename();
                Path filePath = Paths.get("uploads/reviews/" + fileName);
                Files.createDirectories(filePath.getParent());
                Files.write(filePath, media.getBytes());
                String mediaUrl = "/uploads/reviews/" + fileName;

                ReviewMediaDTO mediaDTO = new ReviewMediaDTO();
                mediaDTO.setReviewUrl(mediaUrl);
                mediaDTO.setReviewType(reviewType);
                reviewDTO.setMedia(List.of(mediaDTO));
            }

            ReviewDTO createdReview = reviewService.createReview(reviewDTO);
            return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
        }


        @GetMapping("/orders/{productId}")
        public ResponseEntity<Map<String, Object>> orderCheck(@PathVariable Integer productId) {
            Optional<Integer> orderDetailIdOpt = reviewService.getOrderDetailIdForCurrentUserAndProduct(productId);
            Map<String, Object> response = new HashMap<>();
            response.put("canReview", orderDetailIdOpt.isPresent());
            orderDetailIdOpt.ifPresent(id -> response.put("orderDetailId", id));
            return ResponseEntity.ok(response);
        }

        @GetMapping("/{reviewId}")
        public ResponseEntity<ReviewDTO> getReviewById(@PathVariable Integer reviewId) {
            ReviewDTO review = reviewService.getReviewById(reviewId);
            return ResponseEntity.ok(review);
        }

        @GetMapping("/product/{productId}")
        public ResponseEntity<List<ReviewDTO>> getReviewsByProduct(@PathVariable Integer productId) {
            return ResponseEntity.ok(reviewService.getReviewsByProductId(productId));
        }

        @GetMapping("/user")
        public ResponseEntity<List<ReviewDTO>> getReviewsByCurrentUser() {
            List<ReviewDTO> reviews = reviewService.getReviewsByCurrentUser();
            return ResponseEntity.ok(reviews);
        }

        @PutMapping("/{reviewId}")
        public ResponseEntity<ReviewDTO> updateReview(@PathVariable Integer reviewId,
                                                      @Valid @RequestBody ReviewDTO reviewDTO) {
            ReviewDTO updatedReview = reviewService.updateReview(reviewId, reviewDTO);
            return ResponseEntity.ok(updatedReview);
        }

        @DeleteMapping("/{reviewId}")
        public ResponseEntity<Void> deleteReview(@PathVariable Integer reviewId) {
            reviewService.deleteReview(reviewId);
            return ResponseEntity.noContent().build();
        }

        // Thêm endpoint mới để kiểm tra trạng thái đánh giá của nhiều orderDetailId
        @PostMapping("/orderDetails/check-reviews")
        public ResponseEntity<Map<Integer, Boolean>> checkReviewsForOrderDetails(@RequestBody List<Integer> orderDetailIds) {
            Map<Integer, Boolean> result = reviewService.checkReviewsForOrderDetails(orderDetailIds);
            return ResponseEntity.ok(result);
        }

        // Thêm endpoint để kiểm tra trạng thái đánh giá của một orderDetailId
        @GetMapping("/orderDetails/{orderDetailId}")
        public ResponseEntity<Map<String, Boolean>> checkReviewByOrderDetailId(@PathVariable Integer orderDetailId) {
            boolean reviewed = reviewService.hasReviewForOrderDetail(orderDetailId);
            return ResponseEntity.ok(Map.of("reviewed", reviewed));
        }
    }
