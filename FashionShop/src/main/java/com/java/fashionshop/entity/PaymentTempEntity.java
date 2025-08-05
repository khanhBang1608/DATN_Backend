package com.java.fashionshop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_temp")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTempEntity {

    @Id
    @Column(name = "txn_ref", nullable = false, length = 50)
    private String txnRef;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;


}

