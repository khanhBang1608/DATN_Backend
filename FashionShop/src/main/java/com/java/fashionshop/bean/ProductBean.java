package com.java.fashionshop.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBean {

    private Integer productId; // null khi thêm mới, có khi cập nhật

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;

    @NotBlank(message = "Mô tả sản phẩm không được để trống")
    private String description;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;

    @NotNull(message = "Danh mục không được để trống")
    private Integer categoryId;

    private List<ProductVariantBean> variants; // Danh sách biến thể (có thể null khi thêm mới)
}

