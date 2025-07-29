package com.java.fashionshop.controller;

import com.java.fashionshop.config.PaymentConfig;
import com.java.fashionshop.services.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    // ✅ USER gọi khi nhấn nút "Thanh toán"
    @GetMapping("/api/user/payment/create")
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestParam int total) {
        String paymentUrl = paymentService.createOrder(total);
        return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
    }

    // ✅ PUBLIC - VNPAY redirect về đây, không yêu cầu JWT
    @GetMapping("/api/public/paymentSuccess")
    public String paymentSuccess(@RequestParam Map<String, String> params, Model model) {
        String vnp_SecureHash = params.remove("vnp_SecureHash");

        String hashData = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("&"));

        String computedHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hashData);

        if (computedHash.equals(vnp_SecureHash)) {
            model.addAttribute("message", "Thanh toán thành công!");
        } else {
            model.addAttribute("message", "Thanh toán thất bại hoặc bị thay đổi dữ liệu!");
        }

        // ✅ Redirect về frontend thay vì trả view nếu dùng Vue.js
        return "redirect:http://localhost:5173/payment-success"; // hoặc truyền thêm query: ?status=success
    }
}
