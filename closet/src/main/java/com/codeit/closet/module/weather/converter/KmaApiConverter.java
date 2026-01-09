package com.codeit.closet.module.weather.converter;

import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import com.codeit.closet.module.weather.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 기상청 API 응답을 Entity로 변환
 */
@Slf4j
@Component
public class KmaApiConverter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    /**
     * 초단기실황 응답을 WeatherData로 변환
     */
    public WeatherData convertUltraSrtNcst(
            KmaApiResponse response,
            WeatherRegion weatherRegion
    ) {
        if (response == null || response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null) {
            throw new IllegalArgumentException("잘못된 API 응답 형식");
        }

        List<KmaApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("API 응답에 데이터가 없습니다");
        }

        // Category별로 그룹화
        Map<String, String> categoryMap = items.stream()
                .collect(Collectors.toMap(
                        KmaApiResponse.Item::getCategory,
                        KmaApiResponse.Item::getObsrValue,
                        (v1, v2) -> v1 // 중복 시 첫 번째 값 사용
                ));

        KmaApiResponse.Item firstItem = items.get(0);
        String baseDate = firstItem.getBaseDate();
        String baseTime = firstItem.getBaseTime();

        Instant forecastAt = parseDateTime(baseDate, baseTime);
        Instant forecastedAt = Instant.now();

        return WeatherData.builder()
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(forecastAt)
                .forecastedAt(forecastedAt)
                .skyStatus(parseSkyStatus(categoryMap.get("PTY")))
                .temperatureCurrent(parseDouble(categoryMap.get("T1H")))
                .temperatureCompPrevDay(null) // 조회 시점에 계산됨
                .temperatureMin(parseDouble(categoryMap.get("T1H"))) // 실황이므로 현재값 사용
                .temperatureMax(parseDouble(categoryMap.get("T1H"))) // 실황이므로 현재값 사용
                .precipitationType(parsePrecipitationType(categoryMap.get("PTY")))
                .precipitationAmount(parseDouble(categoryMap.get("RN1")))
                .precipitationProb(0.0) // 실황에는 확률 없음
                .humidityCurrent(parseDouble(categoryMap.get("REH")))
                .humidityComparedToDayBefore(null) // 조회 시점에 계산됨
                .windSpeed(parseDouble(categoryMap.get("WSD")))
                .windAsWord(parseWindStrength(categoryMap.get("WSD")))
                .build();
    }

    /**
     * 단기예보 응답을 WeatherData 리스트로 변환
     * - 3일간 3시간 간격 예보
     */
    public List<WeatherData> convertVilageFcst(
            KmaApiResponse response,
            WeatherRegion weatherRegion
    ) {
        if (response == null || response.getResponse() == null ||
                response.getResponse().getBody() == null ||
                response.getResponse().getBody().getItems() == null) {
            throw new IllegalArgumentException("잘못된 API 응답 형식");
        }

        List<KmaApiResponse.Item> items = response.getResponse().getBody().getItems().getItem();
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("API 응답에 데이터가 없습니다");
        }

        Instant forecastedAt = Instant.now();

        // fcstDate + fcstTime별로 그룹화
        Map<String, List<KmaApiResponse.Item>> groupedByDateTime = items.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getFcstDate() + item.getFcstTime()
                ));

        return groupedByDateTime.entrySet().stream()
                .map(entry -> {
                    String dateTime = entry.getKey();
                    List<KmaApiResponse.Item> itemGroup = entry.getValue();

                    Map<String, String> categoryMap = itemGroup.stream()
                            .collect(Collectors.toMap(
                                    KmaApiResponse.Item::getCategory,
                                    KmaApiResponse.Item::getFcstValue,
                                    (v1, v2) -> v1
                            ));

                    String fcstDate = dateTime.substring(0, 8);
                    String fcstTime = dateTime.substring(8, 12);

                    Instant forecastAt = parseDateTime(fcstDate, fcstTime);

                    return WeatherData.builder()
                            .weatherRegion(weatherRegion)
                            .forecastKind(ForecastKind.SHORT_FCST)
                            .forecastAt(forecastAt)
                            .forecastedAt(forecastedAt)
                            .skyStatus(parseSkyStatusFromSky(categoryMap.get("SKY")))
                            .temperatureCurrent(parseDouble(categoryMap.get("TMP")))
                            .temperatureCompPrevDay(null) // 단기예보에는 전일 비교 데이터 없음
                            .temperatureMin(parseDouble(categoryMap.getOrDefault("TMN", categoryMap.get("TMP"))))
                            .temperatureMax(parseDouble(categoryMap.getOrDefault("TMX", categoryMap.get("TMP"))))
                            .precipitationType(parsePrecipitationType(categoryMap.get("PTY")))
                            .precipitationAmount(parsePrecipitationAmount(categoryMap.get("PCP")))
                            .precipitationProb(parseDouble(categoryMap.get("POP")))
                            .humidityCurrent(parseDouble(categoryMap.get("REH")))
                            .humidityComparedToDayBefore(null) // 단기예보에는 전일 비교 데이터 없음
                            .windSpeed(parseDouble(categoryMap.get("WSD")))
                            .windAsWord(parseWindStrength(categoryMap.get("WSD")))
                            .build();
                })
                .sorted(Comparator.comparing(WeatherData::getForecastAt))
                .toList();
    }

    /**
     * yyyyMMddHHmm 형식의 문자열을 Instant로 변환
     */
    private Instant parseDateTime(String date, String time) {
        String dateTimeStr = date + time;
        LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        return localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }

    /**
     * PTY 코드 → PrecipitationType 변환
     * 0: 없음, 1: 비, 2: 비/눈, 3: 눈, 4: 소나기
     */
    private PrecipitationType parsePrecipitationType(String pty) {
        if (pty == null) return PrecipitationType.NONE;

        return switch (pty) {
            case "0" -> PrecipitationType.NONE;
            case "1" -> PrecipitationType.RAIN;
            case "2" -> PrecipitationType.RAIN_SNOW;
            case "3" -> PrecipitationType.SNOW;
            case "4" -> PrecipitationType.SHOWER;
            default -> PrecipitationType.NONE;
        };
    }

    /**
     * PTY + (SKY) 조합으로 SkyStatus 결정 (초단기실황)
     */
    private SkyStatus parseSkyStatus(String pty) {
        if (pty == null || pty.equals("0")) {
            return SkyStatus.CLEAR; // 강수 없으면 일단 맑음
        }
        return SkyStatus.CLOUDY; // 강수 있으면 흐림
    }

    /**
     * SKY 코드 → SkyStatus 변환 (단기예보)
     * 1: 맑음, 3: 구름많음, 4: 흐림
     */
    private SkyStatus parseSkyStatusFromSky(String sky) {
        if (sky == null) return SkyStatus.CLEAR;

        return switch (sky) {
            case "1" -> SkyStatus.CLEAR;
            case "3" -> SkyStatus.MOSTLY_CLOUDY;
            case "4" -> SkyStatus.CLOUDY;
            default -> SkyStatus.CLEAR;
        };
    }

    /**
     * PCP (1시간 강수량) 문자열 → Double 변환
     * "강수없음", "1mm 미만" 등 처리
     */
    private Double parsePrecipitationAmount(String pcp) {
        if (pcp == null || pcp.equals("강수없음") || pcp.equals("0")) {
            return 0.0;
        }
        if (pcp.contains("미만")) {
            return 0.1;
        }
        // "1mm", "30mm" 등에서 숫자만 추출
        String numStr = pcp.replaceAll("[^0-9.]", "");
        return parseDouble(numStr);
    }

    /**
     * WSD (풍속) → WindStrength 변환
     * 4m/s 미만: WEAK, 4~9m/s: MODERATE, 9m/s 이상: STRONG
     */
    private WindStrength parseWindStrength(String wsd) {
        double speed = parseDouble(wsd);
        if (speed < 4.0) {
            return WindStrength.WEAK;
        } else if (speed < 9.0) {
            return WindStrength.MODERATE;
        } else {
            return WindStrength.STRONG;
        }
    }

    /**
     * 문자열 → Double 변환 (안전)
     */
    private Double parseDouble(String value) {
        if (value == null || value.isEmpty()) {
            return 0.0;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("숫자 변환 실패: {}", value);
            return 0.0;
        }
    }
}
