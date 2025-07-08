package com.java.fashionshop.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryBean {

    private Integer categoryId; // Dùng cho cập nhật, có thể null khi thêm mới

    @NotBlank(message = "Tên danh mục không được để trống")
    private String name;

    @NotNull(message = "Trạng thái không được để trống")
    private Boolean status;

    private Integer parentId; // null nếu không có danh mục cha
}
