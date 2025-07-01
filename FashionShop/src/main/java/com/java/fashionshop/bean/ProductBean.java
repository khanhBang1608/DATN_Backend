package com.java.fashionshop.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductBean {
    private Integer productId; // null khi thêm mới, có khi cập nhật
    private String name;
    private String description;
    private Boolean status;
    private Integer categoryId;
    
    private List<ProductVariantBean> variants; // danh sách biến thể sản phẩm
}
