package com.example.currencyApplication.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FixerConfig {
    @Value("${fixer.api.url}")
    private String apiUrl;

    @Value("${fixer.api.access-key}")
    private String accessKey;

    public String getApiUrl() {
        return apiUrl;
    }

    public String getAccessKey() {
        return accessKey;
    }
}
