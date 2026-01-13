package com.codeit.closet.module.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "closet.kakao.api")
public class KakaoApiProperties {
    private String restKey;
    private String baseUrl;
    private Integer timeout;
}
