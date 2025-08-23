package com.java.fashionshop.services;

import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.ProductEntity;
import com.java.fashionshop.jpa.JpaCartDetail;
import com.java.fashionshop.jpa.JpaFavorite;
import com.java.fashionshop.jpa.JpaProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatProductService {

    @Autowired
    private JpaProduct jpaProduct;

    @Autowired
    private GeminiService geminiService;

    @Autowired
    private JpaFavorite jpaFavorite; // để đếm lượt yêu thích
    @Autowired
    private JpaCartDetail jpaCartDetail; // để đếm lượt trong giỏ

    public String askAboutProduct(String userQuestion) {
        // 1️⃣ Lấy toàn bộ sản phẩm từ DB
        List<ProductEntity> products = jpaProduct.findAll();

        // 2️⃣ Chuyển sang DTO để AI dễ hiểu
        List<ProductDTO> productDTOs = products.stream()
                .map(this::convertToDTO)
                .toList();

        // 3️⃣ Tạo context text cho AI
        String productContext = productDTOs.isEmpty() ? "Không tìm thấy sản phẩm nào trong DB."
                : productDTOs.stream()
                .map(dto -> {
                    String variants = dto.getVariants().stream()
                            .map(v -> String.format("%s - %s - %,.0fđ",
                                    v.getColorName() != null ? v.getColorName() : "-",
                                    v.getSizeName() != null ? v.getSizeName() : "-",
                                    v.getPrice()))
                            .collect(Collectors.joining("; "));
                    return String.format(
                            "* **%s**  \n  - Ngày tạo: %s  \n  - Còn %d biến thể (%s)",
                            dto.getName(),
                            dto.getDateCreated() != null ? dto.getDateCreated().toLocalDate() : "Không rõ",
                            dto.getVariants().size(),
                            variants
                    );
                })
                .collect(Collectors.joining("\n"));

        // 4️⃣ Tạo prompt gửi AI
        String prompt = """
                Đây là dữ liệu sản phẩm trong DB:
                %s

                Người dùng hỏi: %s

                Hãy gợi ý các sản phẩm phù hợp dựa trên dữ liệu DB,
                nếu không có sản phẩm nào phù hợp thì nói rõ.
                """.formatted(productContext, userQuestion);

        // 5️⃣ Gọi Gemini AI
        return geminiService.askGemini(prompt);
    }

    // -----------------------------
    // Hàm convert ProductEntity -> DTO
    // -----------------------------
    private ProductDTO convertToDTO(ProductEntity product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setStatus(product.getStatus());
        dto.setDateCreated(product.getDateCreated());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
            dto.setCategoryStatus(product.getCategory().isStatus());
        }

        Long favoriteCount = jpaFavorite.countByProductId(product.getProductId());
        dto.setFavoriteCount(favoriteCount != null ? favoriteCount.intValue() : 0);

        dto.setViewCount(product.getViewCount() != null ? product.getViewCount() : 0);

        Integer cartCount = jpaCartDetail.countByProductId(product.getProductId());
        dto.setCartCount(cartCount != null ? cartCount : 0);

        if (product.getVariants() != null) {
            List<ProductVariantDTO> variantDTOs = product.getVariants().stream().map(v -> {
                ProductVariantDTO vd = new ProductVariantDTO();
                vd.setProductVariantId(v.getProductVariantId());
                vd.setStock(v.getStock());
                vd.setPrice(v.getPrice());
                vd.setImageName(v.getImageName());
                if (v.getColor() != null) {
                    vd.setColorId(v.getColor().getColorId());
                    vd.setColorName(v.getColor().getColorName());
                }
                if (v.getSize() != null) {
                    vd.setSizeId(v.getSize().getSizeId());
                    vd.setSizeName(v.getSize().getSizeName());
                }
                return vd;
            }).toList();
            dto.setVariants(variantDTOs);
        } else {
            dto.setVariants(List.of());
        }

        return dto;
    }
}
