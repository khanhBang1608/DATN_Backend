package com.java.fashionshop.bean;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordBean {

    // Bước 1: Gửi OTP
    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    // Bước 2: Xác minh OTP
    @Size(min = 6, max = 6, message = "Mã OTP phải gồm 6 chữ số.")
    private String otp;

    // Bước 3: Đặt lại mật khẩu
    @Size(min = 6, message = "Mật khẩu mới phải có ít nhất 6 ký tự.")
    private String newPassword;

    @Size(min = 6, message = "Xác nhận mật khẩu phải có ít nhất 6 ký tự.")
    private String confirmPassword;
}
