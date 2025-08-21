package com.java.fashionshop.helper;

import com.java.fashionshop.entity.AddressEntity;
import com.java.fashionshop.jpa.JpaAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderHelperClass {
    @Autowired
    private static JpaAddress jpaAddress;

    public static AddressEntity findAddressEntityByOrderAddress(String orderAddress, Integer userId) {
        String[] addressParts = orderAddress.split(" - ", 3);
        String customerName = addressParts.length > 0 ? addressParts[0].trim() : "";
        String phone = addressParts.length > 1 ? addressParts[1].trim() : "";
        String detailedAddress = addressParts.length > 2 ? addressParts[2].trim() : "";

        // Phân tích địa chỉ chi tiết để lấy phường/xã, quận/huyện, tỉnh/thành phố
        String[] addressComponents = detailedAddress.split(", ");
        if (addressComponents.length < 3) {
            throw new IllegalArgumentException("Định dạng địa chỉ không hợp lệ: " + detailedAddress);
        }

        String wardName = addressComponents[addressComponents.length - 3].trim(); // Phường/Xã
        String districtName = addressComponents[addressComponents.length - 2].trim(); // Quận/Huyện
        String provinceName = addressComponents[addressComponents.length - 1].replace("Vietnam", "").trim(); // Tỉnh/Thành phố
        String addressDetail = addressComponents[0].trim(); // Địa chỉ chi tiết

        // Tìm AddressEntity phù hợp
        List<AddressEntity> addresses = jpaAddress.findByUser_UserId(userId);
        for (AddressEntity address : addresses) {
            boolean matches = true;

            // So khớp customer_name
            if (!customerName.isEmpty() && !address.getCustomerName().toLowerCase().contains(customerName.toLowerCase())) {
                matches = false;
            }

            // So khớp phone
            if (!phone.isEmpty() && !address.getPhone().equals(phone)) {
                matches = false;
            }

            // So khớp address (địa chỉ chi tiết)
            if (!addressDetail.isEmpty() && !address.getAddress().toLowerCase().contains(addressDetail.toLowerCase())) {
                matches = false;
            }

            // So khớp ward_name
            if (!wardName.isEmpty() && !address.getWardName().toLowerCase().contains(wardName.toLowerCase())) {
                matches = false;
            }

            // So khớp district_name
            if (!districtName.isEmpty() && !address.getDistrictName().toLowerCase().contains(districtName.toLowerCase())) {
                matches = false;
            }

            // So khớp province_name
            if (!provinceName.isEmpty() && !address.getProvinceName().toLowerCase().contains(provinceName.toLowerCase())) {
                matches = false;
            }

            if (matches) {
                return address;
            }
        }

        throw new RuntimeException("Không tìm thấy AddressEntity phù hợp với địa chỉ: " + orderAddress);
    }
}
