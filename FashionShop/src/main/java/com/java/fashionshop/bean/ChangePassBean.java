package com.java.fashionshop.bean;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangePassBean {
	private String currentPassword;
    private String newPassword;
    private String confirmPassword;
}
