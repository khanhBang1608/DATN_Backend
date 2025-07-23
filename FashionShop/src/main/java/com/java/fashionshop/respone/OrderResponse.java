package com.java.fashionshop.respone;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {

    private Integer orderId;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String address;
    private Integer status;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String paymentMethod;
    private Integer paymentStatus;
    private List<OrderDetailResponse> orderDetails;

    @Data
    public static class OrderDetailResponse {
        private Integer orderDetailId;
        private Integer quantity;
        private BigDecimal price;
        private Integer productVariantId;

        public OrderDetailResponse(Integer orderDetailId, Integer quantity, BigDecimal price, Integer productVariantId) {
            this.orderDetailId = orderDetailId;
            this.quantity = quantity;
            this.price = price;
            this.productVariantId = productVariantId;
        }
    }
}