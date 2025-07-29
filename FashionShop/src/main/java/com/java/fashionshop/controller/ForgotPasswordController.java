package com.java.fashionshop.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.java.fashionshop.bean.ForgotPasswordBean;
import com.java.fashionshop.services.EmailService;
import com.java.fashionshop.services.UserService;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.config.PasswordUtil;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ForgotPasswordController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(1000000));
    }

    @PostMapping("/forgot-password")
    public Map<String, Object> forgotPassword(@Valid @RequestBody ForgotPasswordBean form, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("status", "error");
            response.put("errors", result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    e -> e.getField(),
                    e -> e.getDefaultMessage()
                )));
            return response;
        }

        String email = form.getEmail();
        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            response.put("status", "error");
            response.put("errors", Map.of("email", "Email không tồn tại trong hệ thống."));
            return response;
        }

        String otp = generateOtp();
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userService.save(user);

        try {
            emailService.sendEmail(email, "Mã OTP khôi phục mật khẩu", "Mã OTP của bạn là: " + otp);
            response.put("status", "success");
            response.put("message", "Mã OTP đã được gửi đến email của bạn.");
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Không thể gửi email: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/verify-otp")
    public Map<String, Object> verifyOtp(@Valid @RequestBody ForgotPasswordBean form, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("status", "error");
            response.put("errors", result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    e -> e.getField(),
                    e -> e.getDefaultMessage()
                )));
            return response;
        }

        String email = form.getEmail();
        String otp = form.getOtp();

        UserEntity user = userService.findUserByEmail(email);
        if (user == null || user.getOtp() == null || user.getOtpExpiry() == null) {
            response.put("status", "error");
            response.put("message", "Không thể xác minh OTP. Vui lòng yêu cầu lại.");
            return response;
        }

        if (LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            response.put("status", "error");
            response.put("message", "Mã OTP đã hết hạn.");
            return response;
        }

        if (!otp.equals(user.getOtp())) {
            response.put("status", "error");
            response.put("errors", Map.of("otp", "Mã OTP không chính xác."));
            return response;
        }

        user.setOtp(null);
        user.setOtpExpiry(null);
        userService.save(user);

        response.put("status", "success");
        response.put("message", "OTP hợp lệ. Bạn có thể đặt lại mật khẩu.");
        return response;
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@Valid @RequestBody ForgotPasswordBean form, BindingResult result) {
        Map<String, Object> response = new HashMap<>();

        if (result.hasErrors()) {
            response.put("status", "error");
            response.put("errors", result.getFieldErrors().stream()
                .collect(Collectors.toMap(
                    e -> e.getField(),
                    e -> e.getDefaultMessage()
                )));
            return response;
        }

        String email = form.getEmail();
        String newPassword = form.getNewPassword();
        String confirmPassword = form.getConfirmPassword();

        if (!newPassword.equals(confirmPassword)) {
            response.put("status", "error");
            response.put("errors", Map.of("confirmPassword", "Mật khẩu xác nhận không khớp."));
            return response;
        }

        UserEntity user = userService.findUserByEmail(email);
        if (user == null) {
            response.put("status", "error");
            response.put("errors", Map.of("email", "Không tìm thấy người dùng với email này."));
            return response;
        }

        user.setPassword(PasswordUtil.hashPassword(newPassword));
        user.setOtp(null);
        user.setOtpExpiry(null);
        userService.save(user);

        response.put("status", "success");
        response.put("message", "Mật khẩu đã được đặt lại thành công.");
        return response;
    }
}
