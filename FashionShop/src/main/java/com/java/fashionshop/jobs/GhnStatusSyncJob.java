package com.java.fashionshop.jobs;

import com.java.fashionshop.entity.OrderEntity;
import com.java.fashionshop.jpa.JpaOrder;
import com.java.fashionshop.services.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class GhnStatusSyncJob {

    @Autowired
    private OrderService orderService;

    @Autowired
    private JpaOrder orderRepository;

    @Scheduled(fixedRate = 30000)
    public void syncAllGhnOrderStatuses() {
        List<OrderEntity> orders = orderRepository.findByGhnOrderCodeIsNotNull();
        for (OrderEntity order : orders) {
            try {
                orderService.syncGhnOrderStatus(order.getOrderId());
            } catch (Exception e) {
                System.err.println("Lỗi đồng bộ trạng thái đơn hàng " + order.getOrderId() + ": " + e.getMessage());
            }
        }
    }
}
