package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ProductOptionsResponse {
    private List<ColorsDTO> colors;
    private List<SizesDTO> sizes;
}
