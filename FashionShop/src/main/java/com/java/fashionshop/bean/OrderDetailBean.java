package com.java.fashionshop.bean;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderDetailBean {
    private Integer productVariantId;
    private Integer quantity;
    private BigDecimal price;
}
