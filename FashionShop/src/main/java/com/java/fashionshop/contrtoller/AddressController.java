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

        // ✅ Lấy token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid token");
        }

        String token = authHeader.substring(7);
        String email = jwtUtil.extractEmail(token);

        // ✅ Lấy User
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        // ✅ Tạo Address mới
        AddressEntity address = new AddressEntity();
        address.setCustomerName(addressBean.getCustomerName());
        address.setPhone(addressBean.getPhone());
        address.setAddress(addressBean.getAddress());
        address.setProvinceId(addressBean.getProvinceId());
        address.setProvinceName(addressBean.getProvinceName());
        address.setDistrictId(addressBean.getDistrictId());
        address.setDistrictName(addressBean.getDistrictName());
        address.setWardId(addressBean.getWardId());
        address.setWardName(addressBean.getWardName());
        address.setUser(user); // 🔑 Gán UserEntity

        // ✅ Lưu DB
        AddressEntity savedAddress = addressRepository.save(address);

        return ResponseEntity.ok(savedAddress);
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

        List<AddressEntity> addresses = addressRepository.findByUserUserId(user.getUserId());

        return ResponseEntity.ok(addresses);
    }

    @PutMapping("/update/{addressId}")
    public ResponseEntity<?> updateAddress(
            @PathVariable Integer addressId,
            @Valid @RequestBody AddressBean addressBean) {
        return ResponseEntity.ok(addressService.updateAddress(addressId, addressBean));
    }

    @DeleteMapping("/delete/{addressId}")
    public ResponseEntity<?> deleteAddress(@PathVariable Integer addressId) {
        addressService.deleteAddress(addressId);
        return ResponseEntity.ok("Đã xóa địa chỉ thành công");
    }
}
