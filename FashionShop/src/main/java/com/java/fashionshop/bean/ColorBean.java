package com.java.fashionshop.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ColorBean {
    private Integer colorId; // Cho phép null khi thêm mới

    @NotBlank(message = "Tên màu không được để trống")
    @Size(max = 100, message = "Tên màu không được vượt quá 100 ký tự.")
    private String colorName;
}
