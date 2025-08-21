package com.java.fashionshop.services;

import com.java.fashionshop.config.GhnConfig;
import com.java.fashionshop.respone.GhnOrderStatusResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
@Service
public class GhnService {

    @Autowired
    private GhnConfig ghnConfig;

    private final RestTemplate restTemplate = new RestTemplate();

    public GhnOrderStatusResponse getOrderStatus(String orderCode) {
        String url = ghnConfig.getUrl() + "/shipping-order/detail";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Token", ghnConfig.getToken());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("order_code", orderCode);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GhnOrderStatusResponse> response = restTemplate.postForEntity(url, request, GhnOrderStatusResponse.class);
            return response.getBody();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi API GHN: " + e.getMessage());
        }
    }
}