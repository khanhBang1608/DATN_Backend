package com.java.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`Order`") // để tránh conflict với từ khóa SQL
@Data
@NoArgsConstructor
public class OrderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Integer orderId;

	@Column(name = "total_amount", nullable = false)
	private BigDecimal totalAmount;

	@Column(name = "order_date", nullable = false)
	private LocalDateTime orderDate;

	@Column(nullable = false, columnDefinition = "NVARCHAR(255)")
	private String address;

	@Column(nullable = false)
	private Integer status;

	@Column(name = "shipping_fee", nullable = false)
	private BigDecimal shippingFee;

	@Column(name = "discount_amount", nullable = false)
	private BigDecimal discountAmount;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod;

	@Column(name = "payment_status", nullable = false)
	private Integer paymentStatus;

	// Quan hệ với User
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private UserEntity user;

	@Column(name = "discount_code", columnDefinition = "VARCHAR(50)")
	private String discountCode;

	@Column(name = "txn_ref", length = 50, unique = true)
	private String txnRef;

	@Column(name = "idempotency_key" )
	private String idempotencyKey;

	// Quan hệ với OrderDetail
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderDetailEntity> orderDetails = new ArrayList<>();
	
}
