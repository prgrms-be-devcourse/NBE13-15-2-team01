package com.team01.ticketing.kopis;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ticketing.kopis")
public record KopisProperties(String serviceKey, String baseUrl) {

    private static final String DEFAULT_BASE_URL = "http://www.kopis.or.kr/openApi/restful";

    public KopisProperties {
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = DEFAULT_BASE_URL;
        }
    }
}