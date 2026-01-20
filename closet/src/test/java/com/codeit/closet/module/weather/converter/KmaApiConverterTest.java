package com.codeit.closet.module.weather.converter;

import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import com.codeit.closet.module.weather.entity.*;
import com.codeit.closet.module.weather.exception.KmaApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("KmaApiConverter 테스트")
class KmaApiConverterTest {

    private KmaApiConverter converter;
    private WeatherRegion weatherRegion;

    @BeforeEach
    void setUp() {
        converter = new KmaApiConverter();
        weatherRegion = WeatherRegion.builder()
                .id(UUID.randomUUID())
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,종로구,청운동")
                .build();
    }

    private KmaApiResponse createValidUltraSrtNcstResponse() {
        KmaApiResponse response = new KmaApiResponse();
        KmaApiResponse.Response resp = new KmaApiResponse.Response();
        KmaApiResponse.Header header = new KmaApiResponse.Header();
        header.setResultCode("00");
        resp.setHeader(header);

        KmaApiResponse.Body body = new KmaApiResponse.Body();
        KmaApiResponse.Items items = new KmaApiResponse.Items();
        List<KmaApiResponse.Item> itemList = new ArrayList<>();

        // 온도 (T1H)
        itemList.add(createItem("20260120", "1700", "T1H", "15.5"));
        // 습도 (REH)
        itemList.add(createItem("20260120", "1700", "REH", "65"));
        // 강수형태 (PTY)
        itemList.add(createItem("20260120", "1700", "PTY", "0"));
        // 강수량 (RN1)
        itemList.add(createItem("20260120", "1700", "RN1", "0"));
        // 풍속 (WSD)
        itemList.add(createItem("20260120", "1700", "WSD", "2.5"));

        items.setItem(itemList);
        body.setItems(items);
        resp.setBody(body);
        response.setResponse(resp);

        return response;
    }

    private KmaApiResponse createValidVilageFcstResponse() {
        KmaApiResponse response = new KmaApiResponse();
        KmaApiResponse.Response resp = new KmaApiResponse.Response();
        KmaApiResponse.Header header = new KmaApiResponse.Header();
        header.setResultCode("00");
        resp.setHeader(header);

        KmaApiResponse.Body body = new KmaApiResponse.Body();
        KmaApiResponse.Items items = new KmaApiResponse.Items();
        List<KmaApiResponse.Item> itemList = new ArrayList<>();

        // 예보 시간 1: 20260120 1800
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "TMP", "16"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "SKY", "1"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PTY", "0"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "POP", "10"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PCP", "강수없음"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "REH", "55"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "WSD", "3.5"));

        // 예보 시간 2: 20260120 2100
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "TMP", "14"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "SKY", "3"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "PTY", "1"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "POP", "60"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "PCP", "1mm"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "REH", "70"));
        itemList.add(createFcstItem("20260120", "1700", "20260120", "2100", "WSD", "5.5"));

        items.setItem(itemList);
        body.setItems(items);
        resp.setBody(body);
        response.setResponse(resp);

        return response;
    }

    @SuppressWarnings("SameParameterValue")
    private KmaApiResponse.Item createItem(String baseDate, String baseTime, String category, String obsrValue) {
        KmaApiResponse.Item item = new KmaApiResponse.Item();
        item.setBaseDate(baseDate);
        item.setBaseTime(baseTime);
        item.setCategory(category);
        item.setObsrValue(obsrValue);
        item.setNx(60);
        item.setNy(127);
        return item;
    }

    @SuppressWarnings("SameParameterValue")
    private KmaApiResponse.Item createFcstItem(String baseDate, String baseTime,
                                                String fcstDate, String fcstTime,
                                                String category, String fcstValue) {
        KmaApiResponse.Item item = new KmaApiResponse.Item();
        item.setBaseDate(baseDate);
        item.setBaseTime(baseTime);
        item.setFcstDate(fcstDate);
        item.setFcstTime(fcstTime);
        item.setCategory(category);
        item.setFcstValue(fcstValue);
        item.setNx(60);
        item.setNy(127);
        return item;
    }

    @Nested
    @DisplayName("convertUltraSrtNcst - 초단기실황 변환")
    class ConvertUltraSrtNcstTest {

        @Test
        @DisplayName("정상 응답 변환 시 WeatherData 반환")
        void shouldConvertValidResponse() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getWeatherRegion()).isEqualTo(weatherRegion);
            assertThat(result.getForecastKind()).isEqualTo(ForecastKind.ULTRA_NOW);
            assertThat(result.getTemperatureCurrent()).isEqualTo(15.5);
            assertThat(result.getHumidityCurrent()).isEqualTo(65.0);
            assertThat(result.getWindSpeed()).isEqualTo(2.5);
            assertThat(result.getWindAsWord()).isEqualTo(WindStrength.WEAK);
        }

        @Test
        @DisplayName("강수 없음(PTY=0) 변환")
        void shouldConvertNoPrecipitation() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getPrecipitationType()).isEqualTo(PrecipitationType.NONE);
            assertThat(result.getSkyStatus()).isEqualTo(SkyStatus.CLEAR);
        }

        @Test
        @DisplayName("비 강수(PTY=1) 변환")
        void shouldConvertRainPrecipitation() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "PTY".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("1"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getPrecipitationType()).isEqualTo(PrecipitationType.RAIN);
            assertThat(result.getSkyStatus()).isEqualTo(SkyStatus.CLOUDY);
        }

        @Test
        @DisplayName("비/눈(PTY=2) 변환")
        void shouldConvertRainSnowPrecipitation() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "PTY".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("2"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getPrecipitationType()).isEqualTo(PrecipitationType.RAIN_SNOW);
        }

        @Test
        @DisplayName("눈(PTY=3) 변환")
        void shouldConvertSnowPrecipitation() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "PTY".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("3"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getPrecipitationType()).isEqualTo(PrecipitationType.SNOW);
        }

        @Test
        @DisplayName("소나기(PTY=4) 변환")
        void shouldConvertShowerPrecipitation() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "PTY".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("4"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getPrecipitationType()).isEqualTo(PrecipitationType.SHOWER);
        }

        @Test
        @DisplayName("null 응답 시 예외 발생")
        void shouldThrowExceptionWhenResponseIsNull() {
            // When & Then
            assertThatThrownBy(() -> converter.convertUltraSrtNcst(null, weatherRegion))
                    .isInstanceOf(KmaApiException.class)
                    .hasMessageContaining("잘못된 API 응답");
        }

        @Test
        @DisplayName("빈 items 시 예외 발생")
        void shouldThrowExceptionWhenItemsEmpty() {
            // Given
            KmaApiResponse response = new KmaApiResponse();
            KmaApiResponse.Response resp = new KmaApiResponse.Response();
            KmaApiResponse.Body body = new KmaApiResponse.Body();
            KmaApiResponse.Items items = new KmaApiResponse.Items();
            items.setItem(new ArrayList<>());
            body.setItems(items);
            resp.setBody(body);
            response.setResponse(resp);

            // When & Then
            assertThatThrownBy(() -> converter.convertUltraSrtNcst(response, weatherRegion))
                    .isInstanceOf(KmaApiException.class)
                    .hasMessageContaining("데이터가 없습니다");
        }

        @Test
        @DisplayName("약한 바람(4m/s 미만) 변환")
        void shouldConvertWeakWind() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "WSD".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("3.9"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getWindAsWord()).isEqualTo(WindStrength.WEAK);
        }

        @Test
        @DisplayName("보통 바람(4~9m/s) 변환")
        void shouldConvertModerateWind() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "WSD".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("6.0"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getWindAsWord()).isEqualTo(WindStrength.MODERATE);
        }

        @Test
        @DisplayName("강한 바람(9m/s 이상) 변환")
        void shouldConvertStrongWind() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "WSD".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("12.0"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getWindAsWord()).isEqualTo(WindStrength.STRONG);
        }
    }

    @Nested
    @DisplayName("convertVilageFcst - 단기예보 변환")
    class ConvertVilageFcstTest {

        @Test
        @DisplayName("정상 응답 변환 시 WeatherData 리스트 반환")
        void shouldConvertValidResponse() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result).isNotEmpty();
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("맑음(SKY=1) 변환")
        void shouldConvertClearSky() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            WeatherData clearData = result.stream()
                    .filter(d -> d.getSkyStatus() == SkyStatus.CLEAR)
                    .findFirst()
                    .orElse(null);
            assertThat(clearData).isNotNull();
        }

        @Test
        @DisplayName("구름많음(SKY=3) 변환")
        void shouldConvertMostlyCloudySky() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            WeatherData cloudyData = result.stream()
                    .filter(d -> d.getSkyStatus() == SkyStatus.MOSTLY_CLOUDY)
                    .findFirst()
                    .orElse(null);
            assertThat(cloudyData).isNotNull();
        }

        @Test
        @DisplayName("흐림(SKY=4) 변환")
        void shouldConvertCloudySky() {
            // Given
            KmaApiResponse response = new KmaApiResponse();
            KmaApiResponse.Response resp = new KmaApiResponse.Response();
            KmaApiResponse.Body body = new KmaApiResponse.Body();
            KmaApiResponse.Items items = new KmaApiResponse.Items();
            List<KmaApiResponse.Item> itemList = new ArrayList<>();

            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "TMP", "16"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "SKY", "4"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PTY", "0"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "POP", "10"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PCP", "강수없음"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "REH", "55"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "WSD", "3.5"));

            items.setItem(itemList);
            body.setItems(items);
            resp.setBody(body);
            response.setResponse(resp);

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result.get(0).getSkyStatus()).isEqualTo(SkyStatus.CLOUDY);
        }

        @Test
        @DisplayName("강수량 '강수없음' 변환")
        void shouldConvertNoPrecipitationAmount() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            WeatherData noPrecip = result.stream()
                    .filter(d -> d.getPrecipitationAmount() == 0.0)
                    .findFirst()
                    .orElse(null);
            assertThat(noPrecip).isNotNull();
        }

        @Test
        @DisplayName("강수량 '1mm 미만' 변환")
        void shouldConvertLessThan1mmPrecipitation() {
            // Given
            KmaApiResponse response = new KmaApiResponse();
            KmaApiResponse.Response resp = new KmaApiResponse.Response();
            KmaApiResponse.Body body = new KmaApiResponse.Body();
            KmaApiResponse.Items items = new KmaApiResponse.Items();
            List<KmaApiResponse.Item> itemList = new ArrayList<>();

            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "TMP", "16"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "SKY", "1"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PTY", "1"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "POP", "30"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PCP", "1mm 미만"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "REH", "55"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "WSD", "3.5"));

            items.setItem(itemList);
            body.setItems(items);
            resp.setBody(body);
            response.setResponse(resp);

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result.get(0).getPrecipitationAmount()).isEqualTo(0.1);
        }

        @Test
        @DisplayName("강수량 숫자값 변환")
        void shouldConvertNumericPrecipitation() {
            // Given
            KmaApiResponse response = new KmaApiResponse();
            KmaApiResponse.Response resp = new KmaApiResponse.Response();
            KmaApiResponse.Body body = new KmaApiResponse.Body();
            KmaApiResponse.Items items = new KmaApiResponse.Items();
            List<KmaApiResponse.Item> itemList = new ArrayList<>();

            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "TMP", "16"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "SKY", "4"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PTY", "1"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "POP", "80"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "PCP", "15mm"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "REH", "90"));
            itemList.add(createFcstItem("20260120", "1700", "20260120", "1800", "WSD", "7.0"));

            items.setItem(itemList);
            body.setItems(items);
            resp.setBody(body);
            response.setResponse(resp);

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result.get(0).getPrecipitationAmount()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("null 응답 시 예외 발생")
        void shouldThrowExceptionWhenResponseIsNull() {
            // When & Then
            assertThatThrownBy(() -> converter.convertVilageFcst(null, weatherRegion))
                    .isInstanceOf(KmaApiException.class)
                    .hasMessageContaining("잘못된 API 응답");
        }

        @Test
        @DisplayName("빈 items 시 예외 발생")
        void shouldThrowExceptionWhenItemsEmpty() {
            // Given
            KmaApiResponse response = new KmaApiResponse();
            KmaApiResponse.Response resp = new KmaApiResponse.Response();
            KmaApiResponse.Body body = new KmaApiResponse.Body();
            KmaApiResponse.Items items = new KmaApiResponse.Items();
            items.setItem(new ArrayList<>());
            body.setItems(items);
            resp.setBody(body);
            response.setResponse(resp);

            // When & Then
            assertThatThrownBy(() -> converter.convertVilageFcst(response, weatherRegion))
                    .isInstanceOf(KmaApiException.class)
                    .hasMessageContaining("데이터가 없습니다");
        }

        @Test
        @DisplayName("예보 시간 순서대로 정렬됨")
        void shouldReturnSortedByForecastTime() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result).hasSize(2);
            // 첫 번째 결과가 두 번째보다 시간이 빠름
            assertThat(result.get(0).getForecastAt()).isBefore(result.get(1).getForecastAt());
        }

        @Test
        @DisplayName("모든 결과가 SHORT_FCST 타입")
        void shouldHaveShortFcstType() {
            // Given
            KmaApiResponse response = createValidVilageFcstResponse();

            // When
            List<WeatherData> result = converter.convertVilageFcst(response, weatherRegion);

            // Then
            assertThat(result).allMatch(d -> d.getForecastKind() == ForecastKind.SHORT_FCST);
        }
    }

    @Nested
    @DisplayName("parseDouble - 숫자 변환")
    class ParseDoubleTest {

        @Test
        @DisplayName("유효하지 않은 숫자 문자열은 0.0 반환")
        void shouldReturnZeroForInvalidNumber() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "T1H".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue("invalid"));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getTemperatureCurrent()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("빈 문자열은 0.0 반환")
        void shouldReturnZeroForEmptyString() {
            // Given
            KmaApiResponse response = createValidUltraSrtNcstResponse();
            response.getResponse().getBody().getItems().getItem()
                    .stream()
                    .filter(item -> "T1H".equals(item.getCategory()))
                    .findFirst()
                    .ifPresent(item -> item.setObsrValue(""));

            // When
            WeatherData result = converter.convertUltraSrtNcst(response, weatherRegion);

            // Then
            assertThat(result.getTemperatureCurrent()).isEqualTo(0.0);
        }
    }
}
