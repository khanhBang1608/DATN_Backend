package com.java.fashionshop.controller;

import java.io.File;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.java.fashionshop.bean.ProfileBean;
import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.UserDTO;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
		return ResponseEntity.ok(new UserDTO(user.getUserId(), user.getFullName(), user.getEmail(), user.getAvatar(),
				user.getStatus(), user.getRole(),null, user.getDateCreated()));
	}

	@PutMapping("/update")
	public ResponseEntity<?> updateProfile(
	        @Valid @ModelAttribute ProfileBean profileBean,
	        BindingResult bindingResult,
	        HttpServletRequest request) {
	    
	    if (bindingResult.hasErrors()) {
	        String errorMessage = bindingResult.getFieldError("fullName").getDefaultMessage();
	        return ResponseEntity.badRequest().body(errorMessage); 
	    }
	    try {
	        String email = extractEmailFromRequest(request);
	        if (email == null) {
	            return ResponseEntity.status(401).body("Chưa đăng nhập");
	        }

	        Optional<UserEntity> optUser = userService.findByEmail(email);
	        if (optUser.isEmpty()) {
	            return ResponseEntity.status(404).body("Không tìm thấy người dùng");
	        }

	        UserEntity user = optUser.get();
	        user.setFullName(profileBean.getFullName());

	        MultipartFile avatar = profileBean.getAvatar();

	        if (avatar != null && !avatar.isEmpty()) {
	            // Xóa ảnh cũ nếu có
	            if (user.getAvatar() != null && user.getAvatar().contains("/images/")) {
	                String oldFileName = user.getAvatar().substring(user.getAvatar().lastIndexOf("/") + 1);
	                File oldFile = new File(UPLOAD_DIR + oldFileName);
	                try {
	                    if (oldFile.exists()) oldFile.delete();
	                } catch (Exception e) {
	                    System.err.println("Không thể xóa avatar cũ: " + e.getMessage());
	                }
	            }

	            // Tạo thư mục nếu chưa có
	            File uploadDir = new File(UPLOAD_DIR);
	            if (!uploadDir.exists()) uploadDir.mkdirs();

	            // Lưu ảnh mới
	            String fileName = System.currentTimeMillis() + "_" + StringUtils.cleanPath(avatar.getOriginalFilename());
	            File destFile = new File(UPLOAD_DIR + fileName);
	            avatar.transferTo(destFile);

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
	                null,
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
			if (jwtUtil.validateToken(token))
				return jwtUtil.extractEmail(token);
		}
		return null;
	}
}
