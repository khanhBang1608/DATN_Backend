package com.java.fashionshop.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressBean {

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 100, message = "Tên người nhận không được vượt quá 100 ký tự")
    private String customerName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Địa chỉ chi tiết không được để trống")
    private String address;

    @NotNull(message = "Tỉnh/Thành phố không được để trống")
    private Integer provinceId;

    @NotBlank(message = "Tên tỉnh/thành không được để trống")
    private String provinceName;

    @NotNull(message = "Quận/Huyện không được để trống")
    private Integer districtId;

    @NotBlank(message = "Tên quận/huyện không được để trống")
    private String districtName;

    @NotNull(message = "Phường/Xã không được để trống")
    private Integer wardId;

    @NotBlank(message = "Tên phường/xã không được để trống")
    private String wardName;
}
