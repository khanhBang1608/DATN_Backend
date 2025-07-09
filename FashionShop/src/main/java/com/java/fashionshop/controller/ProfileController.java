package com.java.fashionshop.controller;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.UserDTO;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/profile")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ProfileController {

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/images/";

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        String email = extractEmailFromRequest(request);
        if (email == null) {
            return ResponseEntity.status(401).body("Chưa đăng nhập");
        }

        Optional<UserEntity> optUser = userService.findByEmail(email);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("Người dùng không tồn tại");
        }

        UserEntity user = optUser.get();
        return ResponseEntity.ok(new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus(),
                user.getRole(),
                user.getDateCreated()
        ));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            HttpServletRequest request
    ) {
        try {
            String email = extractEmailFromRequest(request);
            if (email == null) return ResponseEntity.status(401).body("Chưa đăng nhập");

            Optional<UserEntity> optUser = userService.findByEmail(email);
            if (optUser.isEmpty()) return ResponseEntity.status(404).body("Không tìm thấy người dùng");

            UserEntity user = optUser.get();
            user.setFullName(fullName);

            if (avatar != null && !avatar.isEmpty()) {
                // Tạo thư mục nếu chưa tồn tại
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) uploadDir.mkdirs();

                // Xóa avatar cũ (nếu là file local, không phải URL từ Google)
                if (user.getAvatar() != null && user.getAvatar().contains("/images/")) {
                    String oldFileName = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
                    File oldFile = new File(UPLOAD_DIR + oldFileName);
                    if (oldFile.exists()) oldFile.delete();
                }

                // Lưu ảnh mới
                String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(avatar.getOriginalFilename());
                File destFile = new File(UPLOAD_DIR + fileName);
                avatar.transferTo(destFile);

                // Trả về đường dẫn đầy đủ cho frontend
                String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
                String avatarUrl = baseUrl + "/images/" + fileName;

                user.setAvatar(avatarUrl);
            }

            userService.save(user);

            return ResponseEntity.ok(new UserDTO(
                    user.getUserId(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getAvatar(),
                    user.getStatus(),
                    user.getRole(),
                    user.getDateCreated()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Cập nhật thất bại: " + e.getMessage());
        }
    }

    private String extractEmailFromRequest(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            if (jwtUtil.validateToken(token)) return jwtUtil.extractEmail(token);
        }
        return null;
    }
}
