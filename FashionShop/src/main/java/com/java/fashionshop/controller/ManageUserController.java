package com.java.fashionshop.controller;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import com.java.fashionshop.dto.AddressDTO;
import com.java.fashionshop.dto.UserDTO;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.services.AddressService;
import com.java.fashionshop.services.UserService;

@RestController
@RequestMapping("/api/admin/users")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ManageUserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private AddressService addressService;

    // ✅ API lấy danh sách tất cả người dùng (trừ role = 0 nếu đúng như bạn filter)
    @GetMapping
    public ResponseEntity<?> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserEntity> userPage = userService.findUsersPaged(page, size);

        List<UserDTO> users = userPage.getContent().stream().map(user -> new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus() != null ? user.getStatus() : false,
                user.getRole(),
                user.getDateCreated()
        )).collect(Collectors.toList());

        return ResponseEntity.ok(
            new java.util.HashMap<String, Object>() {{
                put("users", users);
                put("totalPages", userPage.getTotalPages());
                put("totalElements", userPage.getTotalElements());
                put("currentPage", userPage.getNumber());
            }}
        );
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

    @GetMapping("/{userId}/addresses")
    public ResponseEntity<?> getUserAddresses(@PathVariable Integer userId) {
        List<AddressDTO> addresses = addressService.getAddressesByUserId(userId);
        return ResponseEntity.ok(addresses);
    }


}
