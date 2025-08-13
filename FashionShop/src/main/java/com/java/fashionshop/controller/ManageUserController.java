package com.java.fashionshop.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @GetMapping
    public ResponseEntity<?> getAllUsersPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String fromDate,  // Format: yyyy-MM-dd
            @RequestParam(required = false) String toDate,    // Format: yyyy-MM-dd
            @RequestParam(required = false) Boolean status    // Giữ nguyên tham số status
    ) {
        LocalDateTime from = null;
        LocalDateTime to = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        if (fromDate != null) {
            from = LocalDateTime.parse(fromDate + "T00:00:00");  // Bắt đầu ngày
        }
        if (toDate != null) {
            to = LocalDateTime.parse(toDate + "T23:59:59");  // Kết thúc ngày
        }

        Page<UserEntity> userPage = userService.findUsersPaged(page, size, name, email, from, to, status);

        List<UserDTO> users = userPage.getContent().stream().map(user -> new UserDTO(
                user.getUserId(),
                user.getFullName(),
                user.getEmail(),
                user.getAvatar(),
                user.getStatus() != null ? user.getStatus() : false,
                user.getRole(),
                userService.countOrdersByUserId(user.getUserId()), 
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
