package com.java.fashionshop.controller;

import com.java.fashionshop.bean.AddressBean;
import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.AddressDTO;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.jpa.JpaAddress;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.services.AddressService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/address")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AddressController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private JpaUser userRepository;

    @Autowired
    private JpaAddress addressRepository;

    @Autowired
    private AddressService addressService;

    // ✅ Thêm địa chỉ mới
    @PostMapping("/add")
    public ResponseEntity<?> addAddress(HttpServletRequest request, @Valid @RequestBody AddressBean addressBean) {
        UserEntity user = extractUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized or user not found");
        }

        AddressDTO result = addressService.createAddress(addressBean, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    // ✅ Lấy danh sách địa chỉ
    @GetMapping("/list")
    public ResponseEntity<?> getAddresses(HttpServletRequest request) {
        UserEntity user = extractUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized or user not found");
        }

        List<AddressDTO> addresses = addressService.getAddressesByUserId(user.getUserId());
        return ResponseEntity.ok(addresses);
    }

    // ✅ Cập nhật địa chỉ
    @PutMapping("/update/{addressId}")
    public ResponseEntity<?> updateAddress(
            HttpServletRequest request,
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressBean addressBean) {

        UserEntity user = extractUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized or user not found");
        }

        try {
            AddressDTO updated = addressService.updateAddress(addressId, addressBean, user);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            if ("NO_CHANGE".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.NOT_MODIFIED).body("Không có sự thay đổi nào");
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    // ✅ Xóa địa chỉ
    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(HttpServletRequest request, @PathVariable Integer addressId) {
        UserEntity user = extractUserFromToken(request);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized or user not found");
        }

        addressService.deleteAddress(addressId, user);
        return ResponseEntity.ok("Đã xóa địa chỉ thành công");
    }

    // ✅ Tiện ích: Lấy thông tin người dùng từ token
    private UserEntity extractUserFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);
        return userRepository.findByEmail(email).orElse(null);
    }
}
