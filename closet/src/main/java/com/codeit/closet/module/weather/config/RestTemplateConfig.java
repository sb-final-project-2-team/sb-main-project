package com.codeit.closet.module.weather.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * RestTemplate 설정
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder, WeatherApiProperties properties) {
        return builder
                .connectTimeout(Duration.ofMillis(properties.getTimeout()))
                .readTimeout(Duration.ofMillis(properties.getTimeout()))
                .additionalInterceptors(userAgentInterceptor())
                .build();
    }

    /**
     * User-Agent 헤더를 추가하는 Interceptor
     * 일부 API 서버에서 User-Agent가 없으면 401 Unauthorized를 반환할 수 있음
     */
    private ClientHttpRequestInterceptor userAgentInterceptor() {
        return (request, body, execution) -> {
            request.getHeaders().add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
            request.getHeaders().add("Accept", "application/json, text/html, application/xhtml+xml, application/xml;q=0.9, */*;q=0.8");
            request.getHeaders().add("Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7");
            request.getHeaders().add("Accept-Encoding", "gzip, deflate");
            request.getHeaders().add("Connection", "keep-alive");

            System.out.println("=== RestTemplate Request Headers ===");
            System.out.println("URI: " + request.getURI());
            request.getHeaders().forEach((name, values) ->
                System.out.println(name + ": " + String.join(", ", values))
            );
            System.out.println("===================================");

            return execution.execute(request, body);
        };
    }
}
