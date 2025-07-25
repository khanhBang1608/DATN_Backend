package com.java.fashionshop.bean;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountBean {

    private Integer discountId; // Có thể null khi thêm mới

    @NotBlank(message = "Mã giảm giá không được để trống")
    private String discountCode;


    @NotNull(message = "Phần trăm giảm giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Phần trăm giảm phải lớn hơn 0")
    @DecimalMax(value = "20.0", message = "Phần trăm giảm phải nhỏ hơn hoặc bằng 100")
    private Double discountPercent;

    @NotNull(message = "Giá trị đơn hàng tối thiểu không được để trống")
    @Positive(message = "Giá trị đơn hàng tối thiểu phải lớn hơn 0")
    private Double minOrderAmount;

    @NotNull(message = "Số tiền giảm tối đa không được để trống")
    @Positive(message = "Số tiền giảm tối đa phải lớn hơn 0")
    private Double maxDiscountAmount;

    @NotNull(message = "Giới hạn số lượng không được để trống")
    @Min(value = 1, message = "Giới hạn số lượng phải lớn hơn 0")
    private Integer quantityLimit;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @FutureOrPresent(message = "Ngày bắt đầu phải là hiện tại hoặc tương lai")
    private LocalDate startDate;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @Future(message = "Ngày kết thúc phải là một ngày trong tương lai")
    private LocalDate endDate;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;
}
