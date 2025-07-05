package com.java.fashionshop.component;

import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaUser;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JpaUser userRepository;

    public OAuth2SuccessHandler(JwtUtil jwtUtil, JpaUser userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String email = oauthUser.getAttribute("email");

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found.");
            return;
        }

        String role = String.valueOf(user.getRole());
        
        // 🔐 Tạo JWT token từ JwtUtil
        String token = jwtUtil.generateToken(email, role);
        
        

        // ✅ Gửi token và role về client qua redirect URL
        String redirectUrl = String.format("http://localhost:5173/oauth2/success?token=%s&role=%s", token, role);
        response.sendRedirect(redirectUrl);
    }
}
