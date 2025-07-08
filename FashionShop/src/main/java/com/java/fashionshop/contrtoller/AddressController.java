package com.java.fashionshop.contrtoller;

import com.java.fashionshop.bean.AddressBean;
import com.java.fashionshop.component.JwtUtil;
import com.java.fashionshop.dto.AddressDTO;
import com.java.fashionshop.entity.AddressEntity;
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

    @PostMapping("/add")
    public ResponseEntity<?> addAddress(
            HttpServletRequest request,
            @Valid @RequestBody AddressBean addressBean) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        AddressDTO result = addressService.createAddress(addressBean, user);
        return ResponseEntity.ok(result);
    }


    @GetMapping("/list")
    public ResponseEntity<?> getAddresses(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserEntity user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // ❌ Bỏ trả AddressEntity
        // List<AddressEntity> addresses = addressRepository.findByUserUserId(user.getUserId());

        // ✅ Dùng Service trả DTO
        List<AddressDTO> addresses = addressService.getAddressesByUserId(user.getUserId());

        return ResponseEntity.ok(addresses);
    }


    @PutMapping("/update/{addressId}")
    public ResponseEntity<?> updateAddress(
            HttpServletRequest request,
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressBean addressBean) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        AddressDTO updated = addressService.updateAddress(addressId, addressBean, user);
        return ResponseEntity.ok(updated);
    }


    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(
            HttpServletRequest request,
            @PathVariable Integer addressId) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        addressService.deleteAddress(addressId, user);
        return ResponseEntity.ok("Đã xóa địa chỉ thành công");
    }

}
