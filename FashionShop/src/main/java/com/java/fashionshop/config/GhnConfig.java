package com.java.fashionshop.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "ghn.api")
@Data
public class GhnConfig {
    private String token;
    private String shopId;
    private String fromDistrictId;
    private String url;
    private String shopPhone;
}