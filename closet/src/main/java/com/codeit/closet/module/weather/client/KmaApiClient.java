package com.codeit.closet.module.weather.client;

import com.codeit.closet.module.weather.config.WeatherApiProperties;
import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 기상청 API 클라이언트
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KmaApiClient {

    private final RestTemplate restTemplate;
    private final WeatherApiProperties properties;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HHmm");

    /**
     * 서비스 키를 URL 인코딩
     */
    private String getEncodedServiceKey() {
        return URLEncoder.encode(properties.getServiceKey(), StandardCharsets.UTF_8);
    }

    /**
     * 초단기실황조회
     * - 매시각 40분 이후 호출 가능
     * - 현재 시각 기준 가장 최근 정시 데이터 제공
     *
     * @param nx 격자 X 좌표
     * @param ny 격자 Y 좌표
     * @return 초단기실황 데이터
     */
    public KmaApiResponse getUltraSrtNcst(Integer nx, Integer ny) {
        LocalDateTime now = LocalDateTime.now();

        // 현재 시각이 40분 이전이면 이전 시각 사용
        if (now.getMinute() < 40) {
            now = now.minusHours(1);
        }

        String baseDate = now.format(DATE_FORMATTER);
        String baseTime = now.withMinute(0).format(TIME_FORMATTER);

        java.net.URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getUltraSrtNcstUrl())
                .queryParam("serviceKey", getEncodedServiceKey())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .queryParam("dataType", properties.getDataType())
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        log.info("초단기실황 API 호출: baseDate={}, baseTime={}, nx={}, ny={}",
                baseDate, baseTime, nx, ny);
        log.info("생성된 URI: {}", uri);

        return restTemplate.getForObject(uri, KmaApiResponse.class);
    }

    /**
     * 초단기예보조회
     * - 매시각 45분 이후 호출 가능
     * - 현재 시각부터 6시간 이내 예보 제공
     *
     * @param nx 격자 X 좌표
     * @param ny 격자 Y 좌표
     * @return 초단기예보 데이터
     */
    public KmaApiResponse getUltraSrtFcst(Integer nx, Integer ny) {
        LocalDateTime now = LocalDateTime.now();

        // 현재 시각이 45분 이전이면 이전 시각 사용
        if (now.getMinute() < 45) {
            now = now.minusHours(1);
        }

        String baseDate = now.format(DATE_FORMATTER);
        String baseTime = now.withMinute(30).format(TIME_FORMATTER);

        java.net.URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getUltraSrtFcstUrl())
                .queryParam("serviceKey", getEncodedServiceKey())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 100)
                .queryParam("dataType", properties.getDataType())
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        log.info("초단기예보 API 호출: baseDate={}, baseTime={}, nx={}, ny={}",
                baseDate, baseTime, nx, ny);

        return restTemplate.getForObject(uri, KmaApiResponse.class);
    }

    /**
     * 단기예보조회
     * - 하루 8회 (02, 05, 08, 11, 14, 17, 20, 23시) 발표
     * - 발표 시각 10분 이후 호출 가능
     * - 3일간 예보 제공
     *
     * @param nx 격자 X 좌표
     * @param ny 격자 Y 좌표
     * @return 단기예보 데이터
     */
    public KmaApiResponse getVilageFcst(Integer nx, Integer ny) {
        LocalDateTime now = LocalDateTime.now();

        // 단기예보 발표 시각: 02, 05, 08, 11, 14, 17, 20, 23시
        int[] baseTimes = {2, 5, 8, 11, 14, 17, 20, 23};
        int currentHour = now.getHour();
        int baseHour = 23; // 기본값: 전날 23시

        for (int time : baseTimes) {
            if (currentHour >= time) {
                baseHour = time;
            }
        }

        // 발표 시각 10분 이전이면 이전 발표 시각 사용
        if (currentHour == baseHour && now.getMinute() < 10) {
            // 이전 발표 시각 찾기
            boolean found = false;
            for (int i = baseTimes.length - 1; i >= 0; i--) {
                if (baseTimes[i] < baseHour) {
                    baseHour = baseTimes[i];
                    found = true;
                    break;
                }
            }
            // baseHour가 2(첫 번째 발표 시각)인 경우 전날 23시로 설정
            if (!found) {
                baseHour = 23;
                now = now.minusDays(1);
            }
        }

        String baseDate = now.format(DATE_FORMATTER);
        String baseTime = String.format("%02d00", baseHour);

        java.net.URI uri = UriComponentsBuilder
                .fromHttpUrl(properties.getVilageFcstUrl())
                .queryParam("serviceKey", getEncodedServiceKey())
                .queryParam("pageNo", 1)
                .queryParam("numOfRows", 1000)
                .queryParam("dataType", properties.getDataType())
                .queryParam("base_date", baseDate)
                .queryParam("base_time", baseTime)
                .queryParam("nx", nx)
                .queryParam("ny", ny)
                .build(true)
                .toUri();

        log.info("단기예보 API 호출: baseDate={}, baseTime={}, nx={}, ny={}",
                baseDate, baseTime, nx, ny);

        return restTemplate.getForObject(uri, KmaApiResponse.class);
    }
}
