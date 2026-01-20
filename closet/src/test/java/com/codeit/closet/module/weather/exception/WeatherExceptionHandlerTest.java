package com.codeit.closet.module.weather.exception;

import com.codeit.closet.common.exception.ErrorResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WeatherExceptionHandler 테스트")
class WeatherExceptionHandlerTest {

    private WeatherExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new WeatherExceptionHandler();
    }

    @Nested
    @DisplayName("handleWeatherRegionNotFound - 지역 미등록 예외 처리")
    class HandleWeatherRegionNotFoundTest {

        @Test
        @DisplayName("WeatherRegionNotFoundException 처리 시 404 반환")
        void shouldReturn404ForWeatherRegionNotFound() {
            // Given
            WeatherRegionNotFoundException exception =
                    new WeatherRegionNotFoundException(60, 127);

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleWeatherRegionNotFound(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
        }

        @Test
        @DisplayName("UUID로 생성된 예외 처리")
        void shouldHandleExceptionWithUUID() {
            // Given
            UUID regionId = UUID.randomUUID();
            WeatherRegionNotFoundException exception =
                    new WeatherRegionNotFoundException(regionId);

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleWeatherRegionNotFound(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("handleWeatherDataNotFound - 날씨 데이터 미존재 예외 처리")
    class HandleWeatherDataNotFoundTest {

        @Test
        @DisplayName("WeatherDataNotFoundException 처리 시 404 반환")
        void shouldReturn404ForWeatherDataNotFound() {
            // Given
            WeatherDataNotFoundException exception = new WeatherDataNotFoundException();

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleWeatherDataNotFound(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(404);
        }
    }

    @Nested
    @DisplayName("handleKmaApiException - 기상청 API 예외 처리")
    class HandleKmaApiExceptionTest {

        @Test
        @DisplayName("KmaApiException 처리 시 502 반환")
        void shouldReturn502ForKmaApiException() {
            // Given
            KmaApiException exception = KmaApiException.invalidResponse();

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleKmaApiException(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(502);
        }

        @Test
        @DisplayName("빈 데이터 예외 처리")
        void shouldHandleEmptyDataException() {
            // Given
            KmaApiException exception = KmaApiException.emptyData();

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleKmaApiException(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        }
    }

    @Nested
    @DisplayName("handleKakaoApiException - 카카오 API 예외 처리")
    class HandleKakaoApiExceptionTest {

        @Test
        @DisplayName("KakaoApiException 처리 시 502 반환")
        void shouldReturn502ForKakaoApiException() {
            // Given
            KakaoApiException exception =
                    KakaoApiException.regionNotFound(126.9780, 37.5665);

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleKakaoApiException(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(502);
        }
    }

    @Nested
    @DisplayName("handleWeatherDataCollection - 데이터 수집 예외 처리")
    class HandleWeatherDataCollectionTest {

        @Test
        @DisplayName("WeatherDataCollectionException 처리 시 500 반환")
        void shouldReturn500ForWeatherDataCollectionException() {
            // Given
            WeatherDataCollectionException exception =
                    new WeatherDataCollectionException("API 연결 실패", new RuntimeException("연결 타임아웃"));

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleWeatherDataCollection(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("handleWeatherException - 일반 날씨 예외 처리")
    class HandleWeatherExceptionTest {

        @Test
        @DisplayName("WeatherException 처리 시 400 반환")
        void shouldReturn400ForWeatherException() {
            // Given
            WeatherException exception = new WeatherException("잘못된 요청입니다");

            // When
            ResponseEntity<ErrorResponse> response =
                    exceptionHandler.handleWeatherException(exception);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getStatus()).isEqualTo(400);
        }
    }
}
