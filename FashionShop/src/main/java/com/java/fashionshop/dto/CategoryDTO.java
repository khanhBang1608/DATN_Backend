package com.java.fashionshop.dto;

import lombok.Data;
import java.util.List;

@Data
public class CategoryDTO {
    private Integer categoryId;
    private String categoryName;
    private boolean status;
    private Integer parentId;
    private int productCount;
    private List<CategoryDTO> children;
}
