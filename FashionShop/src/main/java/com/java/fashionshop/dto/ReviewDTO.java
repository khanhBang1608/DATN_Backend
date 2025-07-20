package com.java.fashionshop.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewDTO {
    private Integer reviewId;
    private Integer rating;
    private String comment;
    private LocalDateTime reviewDate;
    private Integer userId;
    private String userFullName;
    private Integer orderDetailId;
    private String productName;
    private List<ReviewMediaDTO> media;
}