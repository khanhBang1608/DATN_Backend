package com.java.fashionshop.controller;

import com.java.fashionshop.entity.AddressEntity;
import com.java.fashionshop.jpa.JpaAddress;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user/shipping")
public class ShippingFeeController {

	private final String ghnToken = "b1128a4b-3c99-11f0-b2d1-fa768adb59a3";
	private final Integer ghnShopId = 5807040;
	private final Integer fromDistrictId = 3317;

	@Autowired
	private JpaAddress jpaAddress;

	@PostMapping("/fee")
	public ResponseEntity<?> getShippingFee(@RequestBody ShippingFeeRequest request) {
		AddressEntity address = jpaAddress.findById(request.getAddressId())
				.orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ"));

		String wardCode = String.valueOf(address.getWardId());
		Integer toDistrictId = address.getDistrictId();

		RestTemplate restTemplate = new RestTemplate();

		// Step 1: Lấy service_id hợp lệ từ GHN
		String serviceUrl = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/available-services";
		HttpHeaders serviceHeaders = new HttpHeaders();
		serviceHeaders.set("Token", ghnToken);
		serviceHeaders.setContentType(MediaType.APPLICATION_JSON);

		Map<String, Object> serviceBody = new HashMap<>();
		serviceBody.put("shop_id", ghnShopId);
		serviceBody.put("from_district", fromDistrictId);
		serviceBody.put("to_district", toDistrictId);

		HttpEntity<Map<String, Object>> serviceRequest = new HttpEntity<>(serviceBody, serviceHeaders);

		ResponseEntity<Map> serviceResponse = restTemplate.exchange(
				serviceUrl,
				HttpMethod.POST,
				serviceRequest,
				Map.class
		);

		Map<String, Object> serviceResponseBody = serviceResponse.getBody();
		if (serviceResponseBody == null || !serviceResponseBody.containsKey("data")) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không tìm thấy dịch vụ giao hàng phù hợp.");
		}

		List<Map<String, Object>> data = (List<Map<String, Object>>) serviceResponseBody.get("data");
		if (data.isEmpty()) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Danh sách dịch vụ rỗng.");
		}

		Integer serviceId = (Integer) data.get(0).get("service_id");
		if (serviceId == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Không lấy được service_id.");
		}

		// Step 2: Tính phí
		String feeUrl = "https://online-gateway.ghn.vn/shiip/public-api/v2/shipping-order/fee";
		HttpHeaders feeHeaders = new HttpHeaders();
		feeHeaders.setContentType(MediaType.APPLICATION_JSON);
		feeHeaders.set("Token", ghnToken);
		feeHeaders.set("ShopId", String.valueOf(ghnShopId));

		Map<String, Object> feeBody = new HashMap<>();
		feeBody.put("from_district_id", fromDistrictId);
		feeBody.put("service_id", serviceId);
		feeBody.put("to_district_id", toDistrictId);
		feeBody.put("to_ward_code", wardCode);
		feeBody.put("height", request.getHeight());
		feeBody.put("length", request.getLength());
		feeBody.put("weight", request.getWeight());
		feeBody.put("width", request.getWidth());
		feeBody.put("insurance_value", request.getInsuranceValue());
		feeBody.put("cod_failed_amount", 0);

		System.out.println("========== GHN SHIPPING FEE REQUEST ==========");
		System.out.println("ServiceId: " + serviceId);
		System.out.println("To District: " + toDistrictId);
		System.out.println("To Ward Code: " + wardCode);
		System.out.println("Request body: " + feeBody);
		System.out.println("=============================================");

		try {
			HttpEntity<Map<String, Object>> feeRequest = new HttpEntity<>(feeBody, feeHeaders);
			ResponseEntity<Map> feeResponse = restTemplate.exchange(feeUrl, HttpMethod.POST, feeRequest, Map.class);
			return ResponseEntity.ok(feeResponse.getBody());
		} catch (Exception ex) {
			ex.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Lỗi khi gọi API GHN (fee): " + ex.getMessage());
		}
	}

	@Data
	public static class ShippingFeeRequest {
		private Integer addressId;
		private Integer weight;
		private Integer length;
		private Integer width;
		private Integer height;
		private Integer insuranceValue;
	}
}
