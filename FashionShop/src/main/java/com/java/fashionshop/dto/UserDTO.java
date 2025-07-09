package com.java.fashionshop.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    private Integer id;
    private String name;
    private String email;
    private String avatar;
    private boolean status;
    private Integer role;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;
}
