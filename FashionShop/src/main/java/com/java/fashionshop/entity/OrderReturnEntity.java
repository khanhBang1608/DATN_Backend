package com.java.fashionshop.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_return_request")
@Data
@NoArgsConstructor
public class OrderReturnEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "image_urls", columnDefinition = "TEXT") // nếu chỉ lưu 1 chuỗi JSON các url
    private String imageUrls;

    @Column(name = "video_urls", columnDefinition = "TEXT")
    private String videoUrls;

    @Column(nullable = false)
    private Integer status;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate = LocalDateTime.now();
}
