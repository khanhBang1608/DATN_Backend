package com.java.fashionshop.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.java.fashionshop.dto.OrderDTO;
import com.java.fashionshop.dto.OrderDetailDTO;
import com.java.fashionshop.entity.OrderEntity;
import com.java.fashionshop.entity.OrderDetailEntity;
import com.java.fashionshop.entity.ProductVariantEntity;
import com.java.fashionshop.entity.UserEntity;
import com.java.fashionshop.entity.DiscountEntity;
import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.jpa.JpaOrderDetail;
import com.java.fashionshop.jpa.JpaUser;
import com.java.fashionshop.jpa.JpaProductVariant;
import com.java.fashionshop.jpa.JpaDiscount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {
    @Autowired
    private JpaOrder orderRepository;
    @Autowired
    private JpaOrderDetail orderDetailRepository;
    @Autowired
    private JpaUser userRepository;
    @Autowired
    private JpaProductVariant productVariantRepository;
    @Autowired
    private JpaDiscount discountRepository;

    private Integer getAuthenticatedUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    return new RuntimeException("Admin user not found");
                });
        if (user.getRole() != 0) {
            throw new RuntimeException("Access denied: Admin role required");
        }
        return user.getUserId();
    }

    public List<OrderDTO> getAllOrders() {
        getAuthenticatedUserId();
        return orderRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public OrderDTO getOrderById(Integer orderId) {
        getAuthenticatedUserId();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    return new RuntimeException("Order not found");
                });
        return convertToDTO(order);
    }

    public OrderDTO createOrder(OrderDTO orderDTO) {
        getAuthenticatedUserId();

        UserEntity user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> {
                    return new RuntimeException("User not found");
                });

        DiscountEntity discount = orderDTO.getDiscountId() != null
                ? discountRepository.findById(orderDTO.getDiscountId())
                .orElseThrow(() -> {
                    return new RuntimeException("Discount not found");
                })
                : null;

        OrderEntity order = new OrderEntity();
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setAddress(orderDTO.getAddress());
        order.setStatus(orderDTO.getStatus());
        order.setShippingFee(orderDTO.getShippingFee());
        order.setDiscountAmount(orderDTO.getDiscountAmount());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(orderDTO.getPaymentStatus());
//        order.setPhoneNumber(orderDTO.getPhoneNumber());
        order.setUser(user);
        order.setDiscount(discount);

        List<OrderDetailEntity> orderDetails = orderDTO.getOrderDetails().stream()
                .map(detailDTO -> {
                    ProductVariantEntity variant = productVariantRepository.findById(detailDTO.getProductVariantId())
                            .orElseThrow(() -> {
                                return new RuntimeException("Product variant not found");
                            });
                    if (variant.getStock() < detailDTO.getQuantity()) {

                        throw new RuntimeException("Insufficient stock");
                    }
                    OrderDetailEntity detail = new OrderDetailEntity();
                    detail.setOrder(order);
                    detail.setProductVariant(variant);
                    detail.setQuantity(detailDTO.getQuantity());
                    detail.setPrice(detailDTO.getPrice());
                    return detail;
                })
                .collect(Collectors.toList());

        order.setOrderDetails(orderDetails);
        orderRepository.save(order);
        return convertToDTO(order);
    }
    public OrderDTO updateOrder(Integer orderId, OrderDTO orderDTO) {
        getAuthenticatedUserId();

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        UserEntity user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        DiscountEntity discount = orderDTO.getDiscountId() != null
                ? discountRepository.findById(orderDTO.getDiscountId())
                .orElseThrow(() -> new RuntimeException("Discount not found"))
                : null;

        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setOrderDate(orderDTO.getOrderDate());
        order.setAddress(orderDTO.getAddress());
        order.setStatus(orderDTO.getStatus());
        order.setShippingFee(orderDTO.getShippingFee());
        order.setDiscountAmount(orderDTO.getDiscountAmount());
        order.setPaymentMethod(orderDTO.getPaymentMethod());
        order.setPaymentStatus(orderDTO.getPaymentStatus());
        order.setUser(user);
        order.setDiscount(discount);
        order.getOrderDetails().clear();
        if (orderDTO.getOrderDetails() != null) {
            for (OrderDetailDTO detailDTO : orderDTO.getOrderDetails()) {
                ProductVariantEntity variant = productVariantRepository.findById(detailDTO.getProductVariantId())
                        .orElseThrow(() -> new RuntimeException("Product variant not found"));

                if (variant.getStock() < detailDTO.getQuantity()) {
                    throw new RuntimeException("Insufficient stock");
                }

                OrderDetailEntity detail = new OrderDetailEntity();
                detail.setOrder(order);
                detail.setProductVariant(variant);
                detail.setQuantity(detailDTO.getQuantity());
                detail.setPrice(detailDTO.getPrice());

                order.getOrderDetails().add(detail);
            }
        }

        orderRepository.save(order);
        return convertToDTO(order);
    }


    public void deleteOrder(Integer orderId) {
        getAuthenticatedUserId();
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    return new RuntimeException("Order not found");
                });
        orderRepository.delete(order);
    }

    private OrderDTO convertToDTO(OrderEntity order) {
        List<OrderDetailDTO> detailDTOs = order.getOrderDetails().stream()
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
                order.getDiscount() != null ? order.getDiscount().getDiscountId() : null,
                detailDTOs,
                order.getUser().getFullName()


        );
    }


    public byte[] exportInvoicePdf(Integer orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        document.add(new Paragraph("HÓA ĐƠN ĐƠN HÀNG #" + order.getOrderId()));
        document.add(new Paragraph("Tên khách hàng: " + order.getUser().getFullName()));
        document.add(new Paragraph("Địa chỉ: " + order.getAddress()));
        document.add(new Paragraph("Tổng tiền: " + order.getTotalAmount() + " VND"));

        for (OrderDetailEntity detail : order.getOrderDetails()) {
            document.add(new Paragraph("- " + detail.getProductVariant().getProduct().getName() +
                    " x" + detail.getQuantity() + " = " + detail.getPrice() + " VND"));
        }

        document.close();
        return outputStream.toByteArray();
    }
}