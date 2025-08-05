package com.java.fashionshop.bean;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileBean {

	@NotBlank(message = "Họ tên không được để trống")
	@Pattern(regexp = "^[\\p{L}\\s]+$", message = "Họ tên không được chứa số hoặc ký tự đặc biệt")
	private String fullName;


    private MultipartFile avatar; 
}
