package com.java.fashionshop.bean;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductPromotionBean {

    private Integer id;

    @NotNull(message = "Số lượng giới hạn không được để trống")
    @Positive(message = "Số lượng giới hạn phải lớn hơn 0")
    private Integer quantityLimit;

    @NotNull(message = "Mã khuyến mãi không được để trống")
    private Integer promotionId;

    @NotNull(message = "Biến thể sản phẩm không được để trống")
    private Integer productVariantId;
}

