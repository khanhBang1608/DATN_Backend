package com.java.fashionshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;

@SpringBootApplication
public class FashionShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionShopApplication.class, args);
	}
	@Bean
	CommandLineRunner initData(JpaCategory categoryRepo) {
	    return args -> {
	        if (categoryRepo.count() == 0) {
	            // Tạo category cha
	            CategoryEntity ao = categoryRepo.save(new CategoryEntity(null, "Áo", true, null, null, null));
	            CategoryEntity quan = categoryRepo.save(new CategoryEntity(null, "Quần", true, null, null, null));

	            // Tạo category con (gán parent)
	            categoryRepo.save(new CategoryEntity(null, "Áo thun", true, ao, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Áo sơ mi", true, ao, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Quần jeans", true, quan, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Quần short", true, quan, null, null));

	            System.out.println("✅ Đã thêm Category & SubCategory mẫu!");
	        }
	    };
	}
}
