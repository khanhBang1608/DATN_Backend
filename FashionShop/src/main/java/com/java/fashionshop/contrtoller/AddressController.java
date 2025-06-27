package com.java.fashionshop.contrtoller;

import com.java.fashionshop.bean.AddressBean;
import com.java.fashionshop.dto.AddressDTO;
import com.java.fashionshop.services.AddressService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping("/add")
    public ResponseEntity<?> addAddress(@Valid @RequestBody AddressBean addressBean) {
        AddressDTO result = addressService.createAddress(addressBean);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getListAddress(@PathVariable Integer userId) {
        return ResponseEntity.ok(addressService.getAddressesByUserId(userId));
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
