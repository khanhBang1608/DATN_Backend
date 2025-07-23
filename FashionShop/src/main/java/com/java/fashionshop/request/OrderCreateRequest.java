package com.java.fashionshop.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateRequest {

    private String address;

    private String paymentMethod;

    private String discountCode;

    private List<OrderDetailRequest> orderDetails;

    @Data
    public static class OrderDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
    }
}