package com.java.fashionshop.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.java.fashionshop.dto.UserDTO;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.UserService;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManageUserController {

    @Autowired
    private UserService userService;

    // ✅ API lấy danh sách tất cả người dùng (trừ role = 0 nếu đúng như bạn filter)
    @GetMapping
    public List<UserDTO> getAllUsers() {
        List<UserEntity> users = userService.findAllUsers();

        return users.stream().map(user -> new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus() != null ? user.getStatus() : false,
                user.getRole(),
                user.getDateCreated() // ✅ Truyền ngày tạo vào
        )).collect(Collectors.toList());
    }



    @PutMapping("/update-status/{id}")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable("id") Integer userId,
            @RequestBody StatusRequest statusRequest
    ) {
        Optional<UserEntity> optUser = userService.findById(userId);
        if (optUser.isEmpty()) {
            return ResponseEntity.status(404).body("Không tìm thấy người dùng");
        }

        UserEntity user = optUser.get();
        user.setStatus(statusRequest.getStatus());
        userService.save(user);
        return ResponseEntity.ok("Cập nhật trạng thái thành công");
    }

    public static class StatusRequest {
        private boolean status;
        public boolean getStatus() {
            return status;
        }
        public void setStatus(boolean status) {
            this.status = status;
        }
    }


}
