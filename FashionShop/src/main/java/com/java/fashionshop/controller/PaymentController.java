package com.java.fashionshop.controller;

import com.java.fashionshop.config.PaymentConfig;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.EmailService;
import com.java.fashionshop.services.OrderService;
import com.java.fashionshop.services.PaymentService;
import com.java.fashionshop.services.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Controller
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private OrderService orderService;

    // ✅ USER gọi khi nhấn nút "Thanh toán"
    @GetMapping("/api/user/payment/create")
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestParam int total) {
        String email = getAuthenticatedEmail();
        String orderInfo = "email=" + URLEncoder.encode(email, StandardCharsets.UTF_8);
        String paymentUrl = paymentService.createOrder(total, orderInfo);
        return ResponseEntity.ok(Collections.singletonMap("paymentUrl", paymentUrl));
    }

    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Không xác thực được người dùng");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }


    // ✅ PUBLIC - VNPAY redirect về đây, không yêu cầu JWT
    @GetMapping("/api/public/paymentSuccess")
    public String paymentSuccess(@RequestParam Map<String, String> params, Model model) {
        String vnp_SecureHash = params.remove("vnp_SecureHash");

        String hashData = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue()))
                .collect(Collectors.joining("&"));


        String computedHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hashData);
        String redirectUrl = "http://localhost:5173/payment-success";

        if (computedHash.equals(vnp_SecureHash)) {
            String transactionStatus = params.get("vnp_TransactionStatus");
            String vnp_Amount = params.get("vnp_Amount");
            String vnp_OrderInfo = params.get("vnp_OrderInfo");

            if ("00".equals(transactionStatus)) {
                try {
                    String userEmail = extractEmailFromOrderInfo(vnp_OrderInfo);
                    int paidAmount = Integer.parseInt(vnp_Amount) / 100;

//                    orderService.createOrderAfterVnpaySuccess(userEmail, paidAmount);

                    String rawPayDate = params.get("vnp_PayDate");
                    DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                    DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                    String formattedPayDate;
                    try {
                        LocalDateTime dateTime = LocalDateTime.parse(rawPayDate, inputFormatter);
                        formattedPayDate = dateTime.format(outputFormatter);
                    } catch (DateTimeParseException e) {
                        formattedPayDate = rawPayDate;
                    }

                    String subject = "Xác nhận thanh toán thành công";
                    String content = String.format("""
    <html>
        <body style="font-family: Arial, sans-serif; padding: 20px;">
            <h2 style="color: #2c3e50;">Thanh toán thành công</h2>
            <p>Cảm ơn bạn đã thanh toán <strong>%d VND</strong>.</p>
            <p><strong>Thời gian thanh toán:</strong> %s</p>
            <p>Nếu có bất kỳ thắc mắc nào, vui lòng liên hệ với bộ phận hỗ trợ của chúng tôi.</p>
            <br/>
            <p style="font-size: 12px; color: #888;">Đây là email tự động, vui lòng không trả lời.</p>
        </body>
    </html>
""", paidAmount, formattedPayDate);

                    emailService.sendEmail(userEmail, subject, content);

                    // Redirect như cũ
                    String queryString = params.entrySet().stream()
                            .map(e -> e.getKey() + "=" + e.getValue())
                            .collect(Collectors.joining("&"));
                    return "redirect:" + redirectUrl + "?" + queryString;
                } catch (Exception e) {
                    System.err.println("Lỗi: " + e.getMessage());
                }
            }

            String queryString = params.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining("&"));
            return "redirect:" + redirectUrl + "?" + queryString;
        } else {
            return "redirect:" + redirectUrl + "?vnp_TransactionStatus=fail";
        }
    }

    private String extractEmailFromOrderInfo(String orderInfo) {
        if (orderInfo != null && orderInfo.startsWith("email=")) {
            try {
                return URLDecoder.decode(orderInfo.substring(6), StandardCharsets.UTF_8.toString());
            } catch (UnsupportedEncodingException e) {
                System.err.println("Error decoding vnp_OrderInfo: " + e.getMessage());
                return null;
            }
        }
        return null;
    }

    private String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8")
                    .replace("+", "%20") // VNPAY không dùng "+"
                    .replace("%21", "!")
                    .replace("%27", "'")
                    .replace("%28", "(")
                    .replace("%29", ")")
                    .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}