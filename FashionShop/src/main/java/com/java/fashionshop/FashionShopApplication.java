package com.java.fashionshop;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.entity.SubCategoryEntity;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaSubCategory;

@SpringBootApplication
public class FashionShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(FashionShopApplication.class, args);
	}

	
	@Bean
	CommandLineRunner initData(JpaCategory categoryRepo, JpaSubCategory subCategoryRepo) {
	    return args -> {
	        // === 1) Insert Category ===
	        if (categoryRepo.count() == 0) {
	            CategoryEntity ao = categoryRepo.save(new CategoryEntity(null, "Áo", null, null));
	            CategoryEntity quan = categoryRepo.save(new CategoryEntity(null, "Quần", null, null));
	            CategoryEntity damVay = categoryRepo.save(new CategoryEntity(null, "Đầm & Váy", null, null));
	            CategoryEntity setDo = categoryRepo.save(new CategoryEntity(null, "Bộ đồ & Set đồ", null, null));
	            CategoryEntity giayDep = categoryRepo.save(new CategoryEntity(null, "Giày dép", null, null));
	            CategoryEntity tuiBalo = categoryRepo.save(new CategoryEntity(null, "Túi xách & Balo", null, null));
	            CategoryEntity phuKien = categoryRepo.save(new CategoryEntity(null, "Phụ kiện", null, null));

	            System.out.println("✅ Đã thêm các Category mẫu!");
	        }

	        // === 2) Insert SubCategory ===
	        if (subCategoryRepo.count() == 0) {
	            // Lấy lại Category từ DB (vì đã có)
	            CategoryEntity ao = categoryRepo.findById(1).orElse(null);
	            CategoryEntity quan = categoryRepo.findById(2).orElse(null);
	            CategoryEntity damVay = categoryRepo.findById(3).orElse(null);
	            CategoryEntity setDo = categoryRepo.findById(4).orElse(null);
	            CategoryEntity giayDep = categoryRepo.findById(5).orElse(null);
	            CategoryEntity tuiBalo = categoryRepo.findById(6).orElse(null);
	            CategoryEntity phuKien = categoryRepo.findById(7).orElse(null);

	            subCategoryRepo.save(new SubCategoryEntity(null, "Áo thun", true, ao, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Áo sơ mi", true, ao, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Áo hoodie", true, ao, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Quần jeans", true, quan, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Quần short", true, quan, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Đầm dự tiệc", true, damVay, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Đầm công sở", true, damVay, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Set thể thao", true, setDo, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Set mặc nhà", true, setDo, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Giày sneaker", true, giayDep, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Sandal", true, giayDep, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Balo laptop", true, tuiBalo, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Túi đeo chéo", true, tuiBalo, null));

	            subCategoryRepo.save(new SubCategoryEntity(null, "Mũ", true, phuKien, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Thắt lưng", true, phuKien, null));
	            subCategoryRepo.save(new SubCategoryEntity(null, "Kính mắt", true, phuKien, null));

	            System.out.println("✅ Đã thêm các SubCategory mẫu!");
	        } else {
	            System.out.println("ℹ️ Bảng SubCategory đã có dữ liệu, bỏ qua insert!");
	        }
	    };
	}
}
