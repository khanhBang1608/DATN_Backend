package com.java.fashionshop.request;

import com.java.fashionshop.entity.OrderDetailEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.query.Order;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaymentRequest {
    private int total;
    private String address;
    private String discountCode;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private List<OrderDetailRequest> orderDetails;
    private String idempotencyKey;
    private Integer addressId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailRequest {
        private Integer productVariantId;
        private Integer quantity;
        private BigDecimal price;
    }
}