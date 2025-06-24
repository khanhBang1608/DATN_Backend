package com.java.fashionshop.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    // Tạo sẵn encoder, hoặc inject từ context nếu muốn (cho đơn giản thì new trực tiếp)
    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * Mã hóa mật khẩu bằng BCrypt
     */
    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    /**
     * So khớp mật khẩu raw với hashed
     */
    public static boolean matches(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }
}
