package com.java.fashionshop.controller;

import com.java.fashionshop.bean.ChangePassBean;
import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.config.PasswordUtil;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class ChangePassController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            HttpServletRequest request,
            @Valid @RequestBody ChangePassBean changepassBean,
            BindingResult bindingResult
    ) {
        // Kiểm tra lỗi validation
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors().get(0).getDefaultMessage());
        }

        // Kiểm tra xác nhận mật khẩu
        if (!changepassBean.getNewPassword().equals(changepassBean.getConfirmPassword())) {
            return ResponseEntity.badRequest().body("Xác nhận mật khẩu mới không khớp.");
        }

        // Lấy token từ header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body("Thiếu token.");
        }

        // Trích xuất email từ token
        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        Optional<UserEntity> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.badRequest().body("Người dùng không tồn tại.");
        }

        UserEntity user = optionalUser.get();

        // Kiểm tra mật khẩu hiện tại
        if (!PasswordUtil.matches(changepassBean.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Mật khẩu hiện tại không đúng.");
        }

        // Cập nhật mật khẩu mới (đã hash)
        String hashedNew = PasswordUtil.hashPassword(changepassBean.getNewPassword());
        user.setPassword(hashedNew);
        userService.save(user);

        return ResponseEntity.ok("Đổi mật khẩu thành công.");
    }
}
