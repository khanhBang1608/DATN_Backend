package com.java.fashionshop.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderDTO {
    private Integer orderId;
    private BigDecimal totalAmount;
    private LocalDateTime orderDate;
    private String address;
    private Integer status;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String paymentMethod;
    private Integer paymentStatus;
    private Integer userId;
    private String discountCode;
    private List<OrderDetailDTO> orderDetails;
    private String userFullName; // Added
    private String ghnOrderStatus;
    private String ghnOrderCode;
//    private String userPhoneNumber;
}