package com.java.fashionshop.services;

import com.java.fashionshop.entity.*;
import com.java.fashionshop.jpa.JpaDiscount;
import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.request.PaymentRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.java.fashionshop.config.PaymentConfig;
import org.springframework.transaction.annotation.Transactional;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class PaymentService {

	@Autowired
	private JpaUser userRepository;

	@Autowired
	private JpaOrder orderRepository;

	@Autowired
	private JpaDiscount discountRepository;

	@Autowired
	private JpaProductVariant productRepository;



	private void adjustStockForOrder(OrderEntity order, boolean isRestock) {
		for (OrderDetailEntity detail : order.getOrderDetails()) {
			ProductVariantEntity variant = detail.getProductVariant();
			int quantity = detail.getQuantity();
			if (isRestock) {
				variant.setStock(variant.getStock() + quantity);
			} else {
				variant.setStock(variant.getStock() - quantity);
			}
			productRepository.save(variant);
		}
	}

	@Transactional
	public Map<String, String> createPendingOrder(
			UserEntity user,
			int totalAmount,
			String address,
			String discountCode,
			BigDecimal discountAmount,
			BigDecimal shippingFee,
			List<PaymentRequest.OrderDetailRequest> orderDetails) {
		if (address == null || address.trim().isEmpty()) {
			throw new IllegalArgumentException("Địa chỉ không được để trống");
		}
		if (orderDetails == null || orderDetails.isEmpty()) {
			throw new IllegalArgumentException("Chi tiết đơn hàng không được để trống");
		}

		String txnRef = PaymentConfig.getRandomNumber(8);
		OrderEntity order = new OrderEntity();
		order.setUser(user);
		order.setTxnRef(txnRef);
		order.setTotalAmount(BigDecimal.valueOf(totalAmount));
		order.setOrderDate(LocalDateTime.now());
		order.setStatus(0); // 0: Pending
		order.setPaymentStatus(0); // 0: Chưa thanh toán
		order.setPaymentMethod("VNPAY");
		order.setAddress(address);
		order.setShippingFee(shippingFee != null ? shippingFee : BigDecimal.ZERO);
		order.setDiscountAmount(discountAmount != null ? discountAmount : BigDecimal.ZERO);

		// Xử lý mã giảm giá
		if (discountCode != null && !discountCode.trim().isEmpty()) {
			DiscountEntity discount = discountRepository.findByDiscountCode(discountCode).orElse(null);
			if (discount != null && discount.getStatus() && discount.getEndDate().isAfter(LocalDate.now())) {
				if (discount.getQuantityLimit() != null && discount.getQuantityLimit() > 0) {
					discount.setQuantityLimit(discount.getQuantityLimit() - 1);
					discountRepository.save(discount);
					order.setDiscountCode(discount.getDiscountCode());
				} else {
					throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
				}
			}
		}

		BigDecimal calculatedTotal = BigDecimal.ZERO;
		for (PaymentRequest.OrderDetailRequest detailRequest : orderDetails) {
			if (detailRequest.getQuantity() <= 0) {
				throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
			}

			ProductVariantEntity variant = productRepository.findById(detailRequest.getProductVariantId())
					.orElseThrow(() -> new EntityNotFoundException(
							"Không tìm thấy product variant với id: " + detailRequest.getProductVariantId()));

			if (variant.getStock() < detailRequest.getQuantity()) {
				throw new RuntimeException("Hết hàng cho variant: " + variant.getProductVariantId());
			}

			BigDecimal price = detailRequest.getPrice();
			if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
				price = variant.getPrice();
			}

			OrderDetailEntity detail = new OrderDetailEntity();
			detail.setOrder(order);
			detail.setQuantity(detailRequest.getQuantity());
			detail.setPrice(price);
			detail.setProductVariant(variant);
			order.getOrderDetails().add(detail);
			calculatedTotal = calculatedTotal.add(price.multiply(new BigDecimal(detailRequest.getQuantity())));
		}

		// Điều chỉnh kho
		adjustStockForOrder(order, false);

		// Xác minh tổng tiền
		BigDecimal expectedTotal = calculatedTotal.add(order.getShippingFee()).subtract(order.getDiscountAmount());
		if (expectedTotal.compareTo(BigDecimal.valueOf(totalAmount)) != 0) {
			throw new IllegalArgumentException("Tổng tiền không khớp với chi tiết đơn hàng");
		}

		orderRepository.save(order);

		// Tạo URL thanh toán
		String orderInfo = "txnRef=" + txnRef;
		String paymentUrl = createPaymentUrl(totalAmount, orderInfo, user.getUserId(), txnRef);

		return Collections.singletonMap("paymentUrl", paymentUrl);
	}

	private String createPaymentUrl(int totalPrice, String orderInfo, Integer userId, String txnRef) {
		String vnp_Version = "2.1.0";
		String vnp_Command = "pay";
		String vnp_TmnCode = PaymentConfig.vnp_TmnCode;
		String orderType = "order-type";
		String vnp_IpAddr = "127.0.0.1";

		Map<String, String> vnp_Params = new HashMap<>();
		vnp_Params.put("vnp_Version", vnp_Version);
		vnp_Params.put("vnp_Command", vnp_Command);
		vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
		vnp_Params.put("vnp_Amount", String.valueOf(totalPrice * 100));
		vnp_Params.put("vnp_CurrCode", "VND");
		vnp_Params.put("vnp_TxnRef", txnRef);
		vnp_Params.put("vnp_OrderInfo", orderInfo);
		vnp_Params.put("vnp_OrderType", orderType);

		String locate = "vn";
		vnp_Params.put("vnp_Locale", locate);
		vnp_Params.put("vnp_ReturnUrl", PaymentConfig.vnp_Returnurl);
		vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

		Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
		String vnp_CreateDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

		cld.add(Calendar.MINUTE, 15);
		String vnp_ExpireDate = formatter.format(cld.getTime());
		vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

		List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
		Collections.sort(fieldNames);
		StringBuilder hashData = new StringBuilder();
		StringBuilder query = new StringBuilder();
		for (Iterator<String> itr = fieldNames.iterator(); itr.hasNext(); ) {
			String fieldName = itr.next();
			String fieldValue = vnp_Params.get(fieldName);
			if (fieldValue != null && !fieldValue.isEmpty()) {
				hashData.append(fieldName).append('=').append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
				query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII)).append('=')
						.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
				if (itr.hasNext()) {
					hashData.append('&');
					query.append('&');
				}
			}
		}

		String vnp_SecureHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hashData.toString());
		return PaymentConfig.vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + vnp_SecureHash;
	}
}
