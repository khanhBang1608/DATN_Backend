package com.java.fashionshop.dto;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaProduct;

import lombok.Data;

@Data
public class SystemProductStatsDTO {
    private int totalVariants;
    private long totalStock;
   
}
