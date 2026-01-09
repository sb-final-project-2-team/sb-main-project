package com.codeit.closet.module.weather.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 기상청 API 설정 프로퍼티
 */
@Configuration
@ConfigurationProperties(prefix = "closet.weather.api")
@Data
public class WeatherApiProperties {

    private String baseUrl;
    private String serviceKey;
    private Integer timeout;
    private String dataType;

    /**
     * 초단기실황조회 엔드포인트
     */
    public String getUltraSrtNcstUrl() {
        return baseUrl + "/getUltraSrtNcst";
    }

    /**
     * 초단기예보조회 엔드포인트
     */
    public String getUltraSrtFcstUrl() {
        return baseUrl + "/getUltraSrtFcst";
    }

    /**
     * 단기예보조회 엔드포인트
     */
    public String getVilageFcstUrl() {
        return baseUrl + "/getVilageFcst";
    }
}
