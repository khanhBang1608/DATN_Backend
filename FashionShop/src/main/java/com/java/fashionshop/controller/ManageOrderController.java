package com.java.fashionshop.controller;

import com.java.fashionshop.dto.OrderDTO;
import com.java.fashionshop.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
public class ManageOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
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

    @GetMapping("/{orderId}/invoice")
    public ResponseEntity<byte[]> downloadInvoice(@PathVariable Integer orderId) {
        byte[] pdfBytes = orderService.exportInvoicePdf(orderId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.builder("attachment")
                .filename("HoaDon-" + orderId + ".pdf")
                .build());

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }
}