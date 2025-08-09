package com.java.fashionshop.controller;

import com.java.fashionshop.dto.OrderDTO;
import com.java.fashionshop.dto.OrderReturnDTO;
import com.java.fashionshop.services.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;


@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class ManageOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderDTO> updateOrder(@PathVariable Integer orderId, @RequestBody OrderDTO orderDTO) {
        return ResponseEntity.ok(orderService.updateOrder(orderId, orderDTO));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer orderId) {
        orderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}/download-pdf")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId) {
        byte[] pdfBytes = orderService.exportInvoicePdf(orderId);

        String fileName = "HoaDon-" + orderId + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.inline()
                        .filename(fileName, StandardCharsets.UTF_8)
                        .build()
        );

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }


    @PutMapping("/{orderId}/approve-return")
    public ResponseEntity<?> acceptReturn(@PathVariable Integer orderId) {
        orderService.acceptReturn(orderId);
        return ResponseEntity.ok("Đã chấp nhận trả hàng và hoàn tiền");
    }


    @PutMapping("/{orderId}/reject-return")
    public ResponseEntity<?> rejectReturn(@PathVariable Integer orderId) {
        orderService.rejectReturn(orderId);
        return ResponseEntity.ok("Đã từ chối yêu cầu trả hàng");
    }

    @GetMapping("/returns/{orderId}")
    public ResponseEntity<OrderReturnDTO> getReturnRequest(@PathVariable Integer orderId) {
        return ResponseEntity.ok(orderService.getReturnRequestByOrderId(orderId));
    }

}