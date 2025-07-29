package com.java.fashionshop.bean;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderBean {
    private BigDecimal totalAmount;
    private String address;
    private Integer status;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private String paymentMethod;
    private Integer paymentStatus;
    private Integer userId;
    private Integer discountId; // optional
    private List<OrderDetailBean> orderDetails;
}
