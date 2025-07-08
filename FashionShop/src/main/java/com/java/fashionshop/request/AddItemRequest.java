package com.java.fashionshop.request;

import lombok.Data;

@Data
public class AddItemRequest {
    private Integer productVariantId;
    private Integer quantity;
}