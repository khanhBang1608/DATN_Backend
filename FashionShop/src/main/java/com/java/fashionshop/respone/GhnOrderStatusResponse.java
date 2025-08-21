package com.java.fashionshop.respone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GhnOrderStatusResponse {
    private String code;
    private String message;
    private GhnOrderData data;

    @Data
    public static class GhnOrderData {
        private String order_code;
        private String status;
        private String last_update_time;
    }
}