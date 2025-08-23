package com.java.fashionshop.controller;

import com.java.fashionshop.dto.OrderDTO;
import com.java.fashionshop.dto.OrderReturnDTO;
import com.java.fashionshop.request.OrderCreateRequest;
import com.java.fashionshop.services.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/user/orders")
@PreAuthorize("hasRole('USER')")
public class OrderController {

	@Autowired
	private OrderService orderService;

	@PostMapping
	public ResponseEntity<OrderDTO> createOrder(@Valid @RequestBody OrderCreateRequest request) {
		OrderDTO response = orderService.createOrder(request);
		return ResponseEntity.ok(response);
	}

	@GetMapping
	public ResponseEntity<List<OrderDTO>> getUserOrders() {
		List<OrderDTO> orders = orderService.getUserOrders();
		return ResponseEntity.ok(orders);
	}

	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDTO> getOrderDetails(@PathVariable Integer orderId) {
		OrderDTO response = orderService.getOrderDetails(orderId);
		return ResponseEntity.ok(response);
	}

	@PutMapping("/{orderId}/cancel")
	public ResponseEntity<String> cancelOrder(@PathVariable Integer orderId) {
		orderService.cancelOrder(orderId);
		return ResponseEntity.ok("Order cancelled successfully");
	}

	@PostMapping("/{orderId}/return-request")
	public ResponseEntity<?> requestReturn(@PathVariable Integer orderId, @RequestParam("reason") String reason,
			@RequestParam(value = "imageUrls", required = false) MultipartFile[] imageFiles,
			@RequestParam(value = "videoUrls", required = false) MultipartFile[] videoFiles) {
		List<String> imageUrls = saveFilesLocally(imageFiles, "images/return");
		List<String> videoUrls = saveFilesLocally(videoFiles, "videos/return");

		OrderReturnDTO dto = new OrderReturnDTO();
		dto.setReason(reason);
		dto.setImageUrls(imageUrls);
		dto.setVideoUrls(videoUrls);

		orderService.requestReturn(orderId, dto);
		return ResponseEntity.ok("Yêu cầu trả hàng đã được gửi.");
	}

	private List<String> saveFilesLocally(MultipartFile[] files, String folderPath) {
		List<String> urls = new ArrayList<>();
		if (files == null || files.length == 0)
			return urls;

		Path uploadDir = Paths.get(folderPath);
		try {
			if (!Files.exists(uploadDir)) {
				Files.createDirectories(uploadDir);
			}

			for (MultipartFile file : files) {
				if (file.isEmpty())
					continue;

				String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
				String filename = System.currentTimeMillis() + "_" + originalFilename;
				Path filePath = uploadDir.resolve(filename);
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				urls.add("/" + folderPath + "/" + filename);
			}
		} catch (IOException e) {
			throw new RuntimeException("Lỗi khi lưu file trả hàng: " + e.getMessage());
		}

		return urls;
	}

	@GetMapping("/{orderId}/return-status")
	public ResponseEntity<?> checkReturnStatus(@PathVariable Integer orderId) {
		boolean isRejected = orderService.isOrderReturnRejected(orderId);

		if (isRejected) {
			return ResponseEntity.ok("Đơn hàng đã bị từ chối yêu cầu trả hàng.");
		} else {
			return ResponseEntity.ok("Đơn hàng chưa bị từ chối hoặc chưa có yêu cầu trả hàng.");
		}
	}

}