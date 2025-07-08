package com.java.fashionshop;

import com.java.fashionshop.component.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.java.fashionshop.entity.CategoryEntity;
import com.java.fashionshop.entity.ColorsEntity;
import com.java.fashionshop.entity.SizesEntity;
import com.java.fashionshop.jpa.JpaCategory;
import com.java.fashionshop.jpa.JpaColors;
import com.java.fashionshop.jpa.JpaSizes;

@SpringBootApplication
public class FashionShopApplication implements CommandLineRunner {
	@Autowired
	private final JwtUtil jwtUtil;

	public FashionShopApplication(JwtUtil jwtUtil) {
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void run(String... args) {
		String token = jwtUtil.generateToken("huythai@gmail.com", "USER");
		System.out.println("Generated token: " + token);
	}
	public static void main(String[] args) {
		SpringApplication.run(FashionShopApplication.class, args);
	}
	@Bean
	CommandLineRunner initData(
	    JpaCategory categoryRepo,
	    JpaSizes sizeRepo,
	    JpaColors colorRepo
	) {
	    return args -> {
	        // Thêm category mẫu
	        if (categoryRepo.count() == 0) {
	            CategoryEntity ao = categoryRepo.save(new CategoryEntity(null, "Áo", true, null, null, null));
	            CategoryEntity quan = categoryRepo.save(new CategoryEntity(null, "Quần", true, null, null, null));
	            CategoryEntity giay = categoryRepo.save(new CategoryEntity(null, "Giày", true, null, null, null));
	            CategoryEntity phuKien = categoryRepo.save(new CategoryEntity(null, "Phụ kiện", true, null, null, null));

	            categoryRepo.save(new CategoryEntity(null, "Áo thun", true, ao, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Áo sơ mi", true, ao, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Quần jeans", true, quan, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Quần short", true, quan, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Giày sneaker", true, giay, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Giày lười", true, giay, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Túi xách", true, phuKien, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Nón", true, phuKien, null, null));
	            categoryRepo.save(new CategoryEntity(null, "Kính mắt", true, phuKien, null, null));

	            System.out.println("✅ Đã thêm Category & SubCategory mẫu!");
	        }

	     // Thêm size mẫu
	        if (sizeRepo.count() == 0) {
	            // Size quần áo
	            String[] clothingSizes = {"S", "M", "L", "XL", "XXL", "Free Size"};
	            for (String size : clothingSizes) {
	                sizeRepo.save(new SizesEntity(null, size));
	            }

	            // Size giày
	            int[] shoeSizes = {36, 37, 38, 39, 40, 41, 42, 43, 44};
	            for (int size : shoeSizes) {
	                sizeRepo.save(new SizesEntity(null, String.valueOf(size)));
	            }

	            // Size vòng tay
	            String[] braceletSizes = {"15cm", "16cm", "17cm", "18cm", "19cm", "20cm"};
	            for (String size : braceletSizes) {
	                sizeRepo.save(new SizesEntity(null, size));
	            }

	            // Size dây chuyền
	            String[] necklaceSizes = {"40cm", "45cm", "50cm", "55cm", "60cm"};
	            for (String size : necklaceSizes) {
	                sizeRepo.save(new SizesEntity(null, size));
	            }

	            // Size nhẫn
	            String[] ringSizes = {"5", "6", "7", "8", "9", "10", "11"};
	            for (String size : ringSizes) {
	                sizeRepo.save(new SizesEntity(null, "Ring " + size));
	            }

	            System.out.println("✅ Đã thêm size quần áo, size giày, vòng tay, dây chuyền, và nhẫn!");
	        }

	        // Thêm màu mẫu
	        if (colorRepo.count() == 0) {
	            String[] colors = {"Đen", "Trắng", "Xám", "Xanh", "Đỏ", "Vàng", "Hồng", "Nâu"};
	            for (String color : colors) {
	                colorRepo.save(new ColorsEntity(null, color));
	            }
	            System.out.println("✅ Đã thêm màu mẫu!");
	        }
	    };
	}

}
