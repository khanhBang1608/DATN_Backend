package com.java.fashionshop.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderReturnDTO {
    private String reason;
    private List<String> imageUrls;
    private List<String> videoUrls;
}
