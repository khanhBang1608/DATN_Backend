package com.java.fashionshop.bean;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantBean {

    private Integer variantId;
    
    private Integer productId;

    @NotNull(message = "Vui lòng chọn màu sắc.")
    private Integer colorId;

    private String colorName;
    
    @NotNull(message = "Vui lòng chọn kích thước.")
    private Integer sizeId;

    private String sizeName;

    @NotNull(message = "Vui lòng nhập giá.")
    @DecimalMin(value = "0.01", inclusive = true, message = "Giá sản phẩm phải lớn hơn 0.")
    private BigDecimal price;

    @NotNull(message = "Vui lòng nhập số lượng tồn kho.")
    @Min(value = 0, message = "Số lượng tồn kho phải lớn hơn hoặc bằng 0.")
    private Integer stock;

    @NotNull(message = "Vui lòng chọn ảnh.")
    private MultipartFile image;
}
