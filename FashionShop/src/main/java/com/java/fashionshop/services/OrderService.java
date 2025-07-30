package com.java.fashionshop.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.java.fashionshop.dto.OrderDTO;
import com.java.fashionshop.dto.OrderDetailDTO;
import com.java.fashionshop.dto.ProductDTO;
import com.java.fashionshop.dto.ProductVariantDTO;
import com.java.fashionshop.entity.*;
import com.java.fashionshop.jpa.JpaDiscount;
import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.jpa.JpaOrderDetail;
import com.java.fashionshop.jpa.JpaProductPromotion;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.request.OrderCreateRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private JpaOrder orderRepository;

    @Autowired
    private JpaUser userRepository;

    @Autowired
    private JpaDiscount discountRepository;

    @Autowired
    private JpaProductVariant productVariantRepository;
    
    @Autowired
    private JpaProductPromotion productPromotionRepository;

    @Autowired
    private JpaOrderDetail jpaOrderDetail;
    
    private Integer getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với email: " + email));
        return user.getUserId();
    }

    @Transactional
    public OrderDTO createOrder(OrderCreateRequest request) {
        Integer userId = getAuthenticatedUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + userId));

        OrderEntity order = new OrderEntity();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setAddress(request.getAddress());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setStatus(0); // 0: Pending
        if ("VNPAY".equalsIgnoreCase(request.getPaymentMethod())) {
            order.setPaymentStatus(1); // 1: Paid
        } else {
            order.setPaymentStatus(0); // 0: Unpaid
        }
        order.setShippingFee(new BigDecimal("10000"));
        order.setDiscountAmount(BigDecimal.ZERO);

        if (request.getDiscountCode() != null) {
            DiscountEntity discount = discountRepository.findByDiscountCode(request.getDiscountCode())
                    .orElse(null);
            if (discount != null && discount.getStatus() && discount.getEndDate().isAfter(LocalDate.now())) {
                if (discount.getQuantityLimit() != null && discount.getQuantityLimit() > 0) {
                    discount.setQuantityLimit(discount.getQuantityLimit() - 1);
                    discountRepository.save(discount);
                    order.setDiscountCode(discount.getDiscountCode());

                    // ✅ Dùng discountAmount từ frontend
                    BigDecimal discountAmount = request.getDiscountAmount() != null
                            ? request.getDiscountAmount()
                            : BigDecimal.ZERO;
                    order.setDiscountAmount(discountAmount);
                } else {
                    throw new RuntimeException("Mã giảm giá đã hết lượt sử dụng");
                }
            }
        }


        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderCreateRequest.OrderDetailRequest detailRequest : request.getOrderDetails()) {
            if (detailRequest.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            ProductVariantEntity variant = productVariantRepository.findById(detailRequest.getProductVariantId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy product variant với id: " + detailRequest.getProductVariantId()));

            if (variant.getStock() < detailRequest.getQuantity()) {
                throw new RuntimeException("Hết hàng cho variant: " + variant.getProductVariantId());
            }

            BigDecimal price = detailRequest.getPrice();
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                price = variant.getPrice(); // fallback nếu thiếu
            }

            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order);
            detail.setQuantity(detailRequest.getQuantity());
            detail.setPrice(price); // ✅ dùng giá từ request nếu có
            detail.setProductVariant(variant);

            order.getOrderDetails().add(detail);
            totalAmount = totalAmount.add(price.multiply(new BigDecimal(detail.getQuantity())));
        }

        order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
        order = orderRepository.save(order);

        return convertToDTO(order);
    }

    public List<OrderDTO> getUserOrders() {
        Integer userId = getAuthenticatedUserId();

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + userId));

        return orderRepository.findByUser(user)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderDetails(Integer orderId) {
        Integer userId = getAuthenticatedUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền truy cập order");
        }

        return convertToDTO(order);
    }

    @Transactional
    public void cancelOrder(Integer orderId) {
        Integer userId = getAuthenticatedUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền hủy order");
        }

        if (order.getStatus() != 0) {
            throw new IllegalStateException("Chỉ có thể hủy order ở trạng thái Pending");
        }

        // Hoàn stock nếu order đã trừ stock (trường hợp bất thường)
        if (order.getStatus() == 3) {
            adjustStockForOrder(order, true);
        }

        order.setStatus(5); // 5: Cancelled
        orderRepository.save(order);
    }

    @Transactional
    public OrderDTO updateOrder(Integer orderId, OrderDTO orderDTO) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        // Lưu trạng thái trước để so sánh
        int previousStatus = order.getStatus();

        order.setAddress(orderDTO.getAddress());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setStatus(orderDTO.getStatus());
        order.setPaymentStatus(orderDTO.getPaymentStatus());
        order.setShippingFee(orderDTO.getShippingFee());
        order.setDiscountAmount(orderDTO.getDiscountAmount());

        if (orderDTO.getUserId() != null) {
            UserEntity user = userRepository.findById(orderDTO.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy user với id: " + orderDTO.getUserId()));
            order.setUser(user);
        }

        if (orderDTO.getDiscountCode() != null) {
            DiscountEntity discount = discountRepository.findByDiscountCode(orderDTO.getDiscountCode())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy discount với id: " + orderDTO.getDiscountCode()));
            order.setDiscountCode(discount.getDiscountCode());
            order.setDiscountAmount(calculateDiscount(order, discount));
        } else {
            order.setDiscountCode(null);
            order.setDiscountAmount(BigDecimal.ZERO);
        }

        // Xử lý OrderDetails
        order.getOrderDetails().clear();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
            if (detailDTO.getQuantity() <= 0) {
                throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
            }

            ProductVariantEntity variant = productVariantRepository.findById(detailDTO.getProductVariantId())
                    .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy product variant với id: " + detailDTO.getProductVariantId()));

            if (variant.getStock() < detailDTO.getQuantity()) {
                throw new RuntimeException("Hết hàng cho variant: " + variant.getProductVariantId());
            }

            OrderDetailEntity detail = new OrderDetailEntity();
            detail.setOrder(order);
            detail.setQuantity(detailDTO.getQuantity());
            detail.setPrice(variant.getPrice());
            detail.setProductVariant(variant);
            order.getOrderDetails().add(detail);
            totalAmount = totalAmount.add(detail.getPrice().multiply(new BigDecimal(detail.getQuantity())));
        }

        if (orderDTO.getStatus() == 1 && previousStatus != 1) {
            adjustStockForOrder(order, false);

            for (OrderDetailEntity detail : order.getOrderDetails()) {
                List<ProductPromotionEntity> promos = productPromotionRepository
                        .findByProductVariant_ProductVariantId(detail.getProductVariant().getProductVariantId());
                for (ProductPromotionEntity promo : promos) {
                    if (promo.getQuantityLimit() != null && promo.getQuantityLimit() >= detail.getQuantity()) {
                        promo.setQuantityLimit(promo.getQuantityLimit() - detail.getQuantity());
                        productPromotionRepository.save(promo);
                    }
                }
            }
        }
        
     // Chỉ trừ stock nếu trạng thái mới là 3 (Delivered) và trạng thái trước không phải 3
        if (orderDTO.getStatus() == 3 && previousStatus != 3) {
            adjustStockForOrder(order, false);
            order.setPaymentStatus(1);
        }
        // Hoàn stock nếu chuyển từ trạng thái 3 sang trạng thái khác
        else if (previousStatus == 3 && orderDTO.getStatus() != 3) {
            adjustStockForOrder(order, true);
            order.setPaymentStatus(0);
        }
        
        order.setTotalAmount(totalAmount.add(order.getShippingFee()).subtract(order.getDiscountAmount()));
        order = orderRepository.save(order);
        return convertToDTO(order);
    }

    @Transactional
    public void deleteOrder(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        // Hoàn stock nếu order ở trạng thái 3
        if (order.getStatus() == 3) {
            adjustStockForOrder(order, true);
        }

        orderRepository.delete(order);
    }

    private void adjustStockForOrder(OrderEntity order, boolean isRestock) {
        for (OrderDetailEntity detail : order.getOrderDetails()) {
            ProductVariantEntity variant = detail.getProductVariant();
            int quantity = detail.getQuantity();
            if (isRestock) {
                variant.setStock(variant.getStock() + quantity);
            } else {
                variant.setStock(variant.getStock() - quantity);
            }
            productVariantRepository.save(variant);
        }
    }

    private BigDecimal calculateDiscount(OrderEntity order, DiscountEntity discount) {
        BigDecimal total = order.getOrderDetails().stream()
                .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal discountAmount = total.multiply(new BigDecimal(discount.getDiscountPercent() / 100.0));
        if (discount.getMaxDiscountAmount() != null) {
            discountAmount = discountAmount.min(new BigDecimal(discount.getMaxDiscountAmount()));
        }
        return discountAmount;
    }

    private OrderDTO convertToDTO(OrderEntity order) {
        List<OrderDetailDTO> orderDetails = order.getOrderDetails().stream()
                .map(detail -> new OrderDetailDTO(
                        detail.getOrderDetailId(),
                        detail.getQuantity(),
                        detail.getPrice(),
                        detail.getProductVariant().getProductVariantId(),
                        detail.getProductVariant().getProduct().getName(),
                        detail.getProductVariant().getImageName(),
                        detail.getProductVariant().getSize().getSizeName(),
                        detail.getProductVariant().getColor().getColorName()
                ))
                .collect(Collectors.toList());

        return new OrderDTO(
                order.getOrderId(),
                order.getTotalAmount(),
                order.getOrderDate(),
                order.getAddress(),
                order.getStatus(),
                order.getShippingFee(),
                order.getDiscountAmount(),
                order.getPaymentMethod(),
                order.getPaymentStatus(),
                order.getUser().getUserId(),
                order.getDiscountCode(),
                orderDetails,
                order.getUser().getFullName()
        );
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));
        return convertToDTO(order);
    }

    public byte[] exportInvoicePdf(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Hóa đơn của đơn hàng #" + order.getOrderId()));
            document.add(new Paragraph("Khách hàng: " + order.getUser().getFullName()));
            document.add(new Paragraph("Địa chỉ: " + order.getAddress()));
            document.add(new Paragraph("Ngày đặt hàng: " + order.getOrderDate()));
            document.add(new Paragraph("Phương thức thanh toán: " + order.getPaymentMethod()));
            document.add(new Paragraph("Trạng thái đơn hàng: " + order.getStatus()));
            document.add(new Paragraph("Tổng tiền: " + order.getTotalAmount()));

            float[] columnWidths = {1, 3, 1, 2};
            Table table = new Table(columnWidths);
            table.addCell("Quantity");
            table.addCell("Product");
            table.addCell("Price");
            table.addCell("Total");

            for (OrderDetailEntity detail : order.getOrderDetails()) {
                table.addCell(String.valueOf(detail.getQuantity()));
                table.addCell(detail.getProductVariant().getProduct().getName());
                table.addCell(String.valueOf(detail.getPrice()));
                table.addCell(String.valueOf(detail.getPrice().multiply(new BigDecimal(detail.getQuantity()))));
            }

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo PDF", e);
        }
    }
    public List<ProductDTO> getTop50BestSellingProducts() {
        List<Object[]> results = orderRepository.findTop50BestSellingProducts();
        return results.stream().limit(50).map(result -> {
            ProductEntity product = (ProductEntity) result[0];
            Long totalSold = (Long) result[1];

            ProductDTO dto = new ProductDTO();
            dto.setProductId(product.getProductId());
            dto.setName(product.getName());
            dto.setDescription(product.getDescription());
            dto.setDateCreated(product.getDateCreated());
            dto.setStatus(product.getStatus());
            dto.setCategoryId(product.getCategory().getCategoryId());
            dto.setCategoryName(product.getCategory().getCategoryName());
            dto.setViewCount(totalSold.intValue()); // Tạm dùng viewCount để lưu totalSold

            List<ProductVariantDTO> variants = product.getVariants().stream()
                .map(v -> {
                    ProductVariantDTO variantDTO = new ProductVariantDTO();
                    variantDTO.setProductVariantId(v.getProductVariantId());
                    variantDTO.setStock(v.getStock());
                    variantDTO.setPrice(v.getPrice());
                    variantDTO.setImageName(v.getImageName());
                    variantDTO.setColorId(v.getColor().getColorId());
                    variantDTO.setColorName(v.getColor().getColorName());
                    variantDTO.setSizeId(v.getSize().getSizeId());
                    variantDTO.setSizeName(v.getSize().getSizeName());
                    return variantDTO;
                })
                .collect(Collectors.toList());
            dto.setVariants(variants);

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void requestReturn(Integer orderId) {
        Integer userId = getAuthenticatedUserId();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (!order.getUser().getUserId().equals(userId)) {
            throw new SecurityException("Không có quyền gửi yêu cầu trả hàng cho đơn này");
        }

        if (order.getStatus() != 3) {
            throw new IllegalStateException("Chỉ có thể yêu cầu trả hàng khi đơn đã giao");
        }

        order.setStatus(4);
        orderRepository.save(order);
    }

    @Transactional
    public void acceptReturn(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (order.getStatus() != 4) {
            throw new IllegalStateException("Chỉ xử lý đơn đang ở trạng thái yêu cầu trả hàng");
        }

        order.setStatus(6);
        order.setPaymentStatus(0); 
        adjustStockForOrder(order, true);
        orderRepository.save(order);
    }

    @Transactional
    public void rejectReturn(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy order với id: " + orderId));

        if (order.getStatus() != 4) {
            throw new IllegalStateException("Chỉ xử lý đơn đang ở trạng thái yêu cầu trả hàng");
        }

        order.setStatus(7);
        orderRepository.save(order);
    }
    public Long getTotalSoldQuantityByProductId(Integer productId) {
        Long totalSold = jpaOrderDetail.getTotalSoldQuantityByProductId(productId);
        return totalSold != null ? totalSold : 0L;
    }

}
