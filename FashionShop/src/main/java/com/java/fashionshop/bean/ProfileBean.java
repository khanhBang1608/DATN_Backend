package com.java.fashionshop.bean;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileBean {

    @NotBlank(message = "Họ tên không được để trống")
    private String fullName;

    private MultipartFile avatar; 
}
