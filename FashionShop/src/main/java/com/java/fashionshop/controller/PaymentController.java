package com.java.fashionshop.controller;

import com.java.fashionshop.config.PaymentConfig;
import com.java.fashionshop.entity.OrderDetailEntity;
import com.java.fashionshop.entity.OrderEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaOrderDetail;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.request.PaymentRequest;
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
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
@Controller
public class PaymentController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private JpaOrderDetail orderDetailRepository;

    @Autowired
    private JpaUser userRepository;
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/api/user/payment/create")
    @ResponseBody
    public ResponseEntity<?> createPayment(@RequestBody PaymentRequest request) {
        String email = getAuthenticatedEmail();
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Địa chỉ không được để trống");
        }
        if (request.getOrderDetails() == null || request.getOrderDetails().isEmpty()) {
            return ResponseEntity.badRequest().body("Chi tiết đơn hàng không được để trống");
        }

        Map<String, String> response = paymentService.createPendingOrder(
                user,
                request.getTotal(),
                request.getAddress(),
                request.getDiscountCode(),
                request.getDiscountAmount(),
                request.getShippingFee(),
                request.getOrderDetails(),
                request.getIdempotencyKey()
        );
        return ResponseEntity.ok(response);
    }
    private String getAuthenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("Không xác thực được người dùng");
        }
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userDetails.getUsername();
    }
    @GetMapping("/api/public/paymentSuccess")
    public String paymentSuccess(@RequestParam Map<String, String> params) {
        String vnp_SecureHash = params.remove("vnp_SecureHash");

        String hashData = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + urlEncode(e.getValue()))
                .collect(Collectors.joining("&"));
        String computedHash = PaymentConfig.hmacSHA512(PaymentConfig.vnp_HashSecret, hashData);
        String redirectUrl = "http://localhost:5173/payment-success";

        if (!computedHash.equals(vnp_SecureHash)) {
            return "redirect:" + redirectUrl + "?vnp_TransactionStatus=fail";
        }

        String txnRef = params.get("vnp_TxnRef");
        String transactionStatus = params.get("vnp_TransactionStatus");

        if ("00".equals(transactionStatus)) {
            try {
                OrderEntity order = orderService.findByTxnRef(txnRef);
                order.setPaymentStatus(1);
                order.setStatus(1);
                orderService.save(order);

                DecimalFormat df = new DecimalFormat("#,##0 VND");
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

                StringBuilder tableRows = new StringBuilder();
                int index = 1;
                BigDecimal subtotal = BigDecimal.ZERO;
                for (OrderDetailEntity d : orderDetailRepository.findByOrder_OrderId(order.getOrderId())) {
                    BigDecimal total = d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity()));
                    subtotal = subtotal.add(total);
                    tableRows.append(String.format("""
                    <tr>
                        <td style="border: 1px solid #d3d3d3; padding: 8px; text-align: center; font-size: 10px;">%d</td>
                        <td style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px;">%s (%s, %s)</td>
                        <td style="border: 1px solid #d3d3d3; padding: 8px; text-align: center; font-size: 10px;">%d</td>
                        <td style="border: 1px solid #d3d3d3; padding: 8px; text-align: right; font-size: 10px;">%s</td>
                        <td style="border: 1px solid #d3d3d3; padding: 8px; text-align: right; font-size: 10px;">%s</td>
                    </tr>
                """, index++,
                            d.getProductVariant().getProduct().getName(),
                            d.getProductVariant().getSize().getSizeName(),
                            d.getProductVariant().getColor().getColorName(),
                            d.getQuantity(),
                            df.format(d.getPrice()),
                            df.format(total)));
                }

                // Tóm tắt chi phí
                StringBuilder summaryRows = new StringBuilder();
                summaryRows.append(String.format("""
                <tr>
                    <td style="padding: 8px; font-size: 10px;">Tạm tính</td>
                    <td style="padding: 8px; text-align: right; font-size: 10px;">%s</td>
                </tr>
            """, df.format(subtotal)));

                summaryRows.append(String.format("""
                <tr>
                    <td style="padding: 8px; font-size: 10px;">Phí vận chuyển</td>
                    <td style="padding: 8px; text-align: right; font-size: 10px;">%s</td>
                </tr>
            """, df.format(order.getShippingFee())));

                summaryRows.append(String.format("""
                <tr>
                    <td style="padding: 8px; font-size: 10px;">Giảm giá</td>
                    <td style="padding: 8px; text-align: right; font-size: 10px;">-%s</td>
                </tr>
            """, df.format(order.getDiscountAmount())));

                summaryRows.append(String.format("""
                <tr>
                    <td style="padding: 8px; font-size: 12px; font-weight: bold;">Tổng cộng</td>
                    <td style="padding: 8px; text-align: right; font-size: 12px; font-weight: bold;">%s</td>
                </tr>
            """, df.format(order.getTotalAmount())));

                String subject = "Hóa đơn thanh toán thành công - Đơn #" + order.getOrderId();
                String content = String.format("""
                <html>
                <body style="font-family: Arial, sans-serif; color: #333;">
                    <h2 style="color: #0066cc; text-align: center; font-size: 20px; font-weight: bold; margin-bottom: 10px;">HÓA ĐƠN BÁN HÀNG</h2>
                    <p style="font-size: 12px; font-weight: bold; text-align: left;">CÔNG TY CỔ PHẦN MAISON RETAIL MANAGEMENT INTERNATIONAL</p>
                    <p style="font-size: 10px; text-align: left;">Địa chỉ: Toà nhà FPT Polytechnic, Đ. Số 22, Thường Thạnh, Cái Răng, Cần Thơ</p>
                    <p style="font-size: 10px; text-align: left; margin-bottom: 20px;">Số điện thoại: 0378 447 716 | Email: customers@lhex.vn</p>
                    <div style="border-bottom: 1px solid #0066cc; margin-bottom: 10px;"></div>
                    <table style="width: 100%%; font-size: 10px; margin-bottom: 20px;">
                        <tr>
                            <td style="width: 50%%;"><strong>Mã hóa đơn:</strong> #%s</td>
                            <td style="width: 50%%; text-align: right;"><strong>Ngày xuất:</strong> %s</td>
                        </tr>
                        <tr>
                            <td><strong>Khách hàng:</strong> %s</td>
                            <td style="text-align: right;"><strong>Ngày đặt hàng:</strong> %s</td>
                        </tr>
                        <tr>
                            <td><strong>Địa chỉ:</strong> %s</td>
                            <td style="text-align: right;"><strong>Phương thức thanh toán:</strong> %s</td>
                        </tr>
                        <tr>
                            <td><strong>Trạng thái thanh toán:</strong> %s</td>
                            <td></td>
                        </tr>
                    </table>
                    <h3 style="font-size: 14px; font-weight: bold;">Chi tiết đơn hàng</h3>
                    <table style="border-collapse: collapse; width: 100%%;">
                        <thead>
                            <tr style="background-color: #0066cc; color: white;">
                                <th style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px; text-align: center;">STT</th>
                                <th style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px; text-align: center;">Sản phẩm</th>
                                <th style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px; text-align: center;">Số lượng</th>
                                <th style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px; text-align: center;">Đơn giá</th>
                                <th style="border: 1px solid #d3d3d3; padding: 8px; font-size: 10px; text-align: center;">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    <br/>
                    <h3 style="font-size: 14px; font-weight: bold;">Tóm tắt chi phí</h3>
                    <table style="width: 50%%; float: left;">
                        <tbody>
                            %s
                        </tbody>
                    </table>
                    <div style="clear: both;"></div>
                    <p style="font-size: 10px; text-align: center; color: #333; margin-top: 20px;">
                        Cảm ơn quý khách đã mua sắm tại L'Hex Shop!<br>
                        Vui lòng liên hệ hỗ trợ qua email customers@lhex.vn hoặc hotline 0378 447 716.
                    </p>
                </body>
                </html>
            """, order.getOrderId(),
                        LocalDate.now().format(dateFormatter),
                        order.getUser().getFullName(),
                        order.getOrderDate().format(dateFormatter),
                        order.getAddress(),
                        order.getPaymentMethod(),
                        order.getPaymentStatus() == 1 ? "Đã thanh toán" : "Chưa thanh toán",
                        tableRows.toString(),
                        summaryRows.toString());

                emailService.sendEmail(order.getUser().getEmail(), subject, content);

            } catch (MessagingException e) {
                e.printStackTrace();
            }
        }

        String queryString = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return "redirect:" + redirectUrl + "?" + queryString;
    }

    private String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, "UTF-8")
                    .replace("+", "%20")
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