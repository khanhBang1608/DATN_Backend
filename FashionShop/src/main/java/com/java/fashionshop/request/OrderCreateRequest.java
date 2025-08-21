package com.java.fashionshop.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderCreateRequest {

    private String address;

    private String paymentMethod;

    private String discountCode;
    
    private BigDecimal discountAmount;
    
    private BigDecimal ShippingFee;

    private List<OrderDetailRequest> orderDetails;

    private String idempotencyKey;
    private Integer status;
    private Integer addressId;
 
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
        private BigDecimal price;
       
    }
}