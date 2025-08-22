package com.java.fashionshop.bean;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionBean {

    private Integer id;

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    private String code;

    @NotBlank(message = "Mô tả không được để trống")
    private String description;

    @NotNull(message = "Số phần trăm giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Phần trăm giảm giá phải lớn hơn 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Phần trăm giảm giá không được vượt quá 100%")
    private Double discountAmount;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    private LocalDateTime startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    private LocalDateTime endDate;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;
}

