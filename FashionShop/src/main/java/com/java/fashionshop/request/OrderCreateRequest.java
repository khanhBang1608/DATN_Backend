package com.java.fashionshop.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateRequest {

    private String address;

    private String paymentMethod;

    private String discountCode;
    
    private BigDecimal discountAmount;

    private List<OrderDetailRequest> orderDetails;
    private Integer status;
 
    @Data
    public static class OrderDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
        private BigDecimal price;
       
    }
}