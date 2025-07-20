package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromotionDTO {

    private Integer id;

    private String code;

    private String description;

    private Double discountAmount;

    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean status;

    // Nếu bạn muốn trả luôn các sản phẩm áp dụng:
    private List<ProductPromotionDTO> productPromotions;
}
