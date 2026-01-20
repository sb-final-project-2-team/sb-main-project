package com.codeit.closet.module.weather.client;

import com.codeit.closet.module.weather.config.KakaoApiProperties;
import com.codeit.closet.module.weather.exception.KakaoApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoApiClient 테스트")
class KakaoApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private KakaoApiProperties kakaoApiProperties;

    private KakaoApiClient kakaoApiClient;

    @BeforeEach
    void setUp() {
        kakaoApiClient = new KakaoApiClient(restTemplate, kakaoApiProperties);
    }

    @Nested
    @DisplayName("getRegionByCoordinate - 좌표로 지역 조회")
    class GetRegionByCoordinateTest {

        @Test
        @DisplayName("정상 응답 시 지역명 반환")
        void shouldReturnAddressName() {
            // Given
            Double longitude = 126.9780;
            Double latitude = 37.5665;

            KakaoApiClient.Document document = new KakaoApiClient.Document("서울특별시 종로구 청운동");
            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document));

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-rest-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When
            String result = kakaoApiClient.getRegionByCoordinate(longitude, latitude);

            // Then
            assertThat(result).isEqualTo("서울특별시 종로구 청운동");
        }

        @Test
        @DisplayName("API 호출 시 올바른 Authorization 헤더 사용")
        void shouldUseCorrectAuthorizationHeader() {
            // Given
            Double longitude = 126.9780;
            Double latitude = 37.5665;

            KakaoApiClient.Document document = new KakaoApiClient.Document("서울특별시 종로구 청운동");
            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document));

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("my-api-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When
            kakaoApiClient.getRegionByCoordinate(longitude, latitude);

            // Then
            ArgumentCaptor<HttpEntity<?>> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
            verify(restTemplate).exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    entityCaptor.capture(),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            );

            HttpEntity<?> capturedEntity = entityCaptor.getValue();
            assertThat(capturedEntity.getHeaders().getFirst("Authorization"))
                    .isEqualTo("KakaoAK my-api-key");
        }

        @Test
        @DisplayName("API 호출 시 올바른 좌표 파라미터 사용")
        void shouldUseCorrectCoordinateParameters() {
            // Given
            Double longitude = 127.123;
            Double latitude = 37.456;

            KakaoApiClient.Document document = new KakaoApiClient.Document("경기도 성남시");
            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document));

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When
            kakaoApiClient.getRegionByCoordinate(longitude, latitude);

            // Then
            ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
            verify(restTemplate).exchange(
                    uriCaptor.capture(),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            );

            String uriString = uriCaptor.getValue().toString();
            assertThat(uriString).contains("x=" + longitude);
            assertThat(uriString).contains("y=" + latitude);
        }

        @Test
        @DisplayName("응답 body가 null일 때 예외 발생")
        void shouldThrowExceptionWhenBodyIsNull() {
            // Given
            Double longitude = 126.9780;
            Double latitude = 37.5665;

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(null, HttpStatus.OK));

            // When & Then
            assertThatThrownBy(() -> kakaoApiClient.getRegionByCoordinate(longitude, latitude))
                    .isInstanceOf(KakaoApiException.class)
                    .hasMessageContaining("지역 정보가 존재하지 않습니다");
        }

        @Test
        @DisplayName("documents가 비어있을 때 예외 발생")
        void shouldThrowExceptionWhenDocumentsEmpty() {
            // Given
            Double longitude = 126.9780;
            Double latitude = 37.5665;

            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of());

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When & Then
            assertThatThrownBy(() -> kakaoApiClient.getRegionByCoordinate(longitude, latitude))
                    .isInstanceOf(KakaoApiException.class)
                    .hasMessageContaining(longitude.toString())
                    .hasMessageContaining(latitude.toString());
        }

        @Test
        @DisplayName("첫 번째 document의 address_name 반환")
        void shouldReturnFirstDocumentAddressName() {
            // Given
            Double longitude = 126.9780;
            Double latitude = 37.5665;

            KakaoApiClient.Document document1 = new KakaoApiClient.Document("첫 번째 주소");
            KakaoApiClient.Document document2 = new KakaoApiClient.Document("두 번째 주소");
            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document1, document2));

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When
            String result = kakaoApiClient.getRegionByCoordinate(longitude, latitude);

            // Then
            assertThat(result).isEqualTo("첫 번째 주소");
        }

        @Test
        @DisplayName("다양한 좌표로 정상 호출")
        void shouldWorkWithVariousCoordinates() {
            // Given
            Double longitude = 129.0756;
            Double latitude = 35.1796;

            KakaoApiClient.Document document = new KakaoApiClient.Document("부산광역시 해운대구");
            KakaoApiClient.KakaoRegionResponse responseBody =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document));

            given(kakaoApiProperties.getBaseUrl()).willReturn("https://dapi.kakao.com");
            given(kakaoApiProperties.getRestKey()).willReturn("test-key");
            given(restTemplate.exchange(
                    any(URI.class),
                    eq(HttpMethod.GET),
                    any(HttpEntity.class),
                    eq(KakaoApiClient.KakaoRegionResponse.class)
            )).willReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));

            // When
            String result = kakaoApiClient.getRegionByCoordinate(longitude, latitude);

            // Then
            assertThat(result).isEqualTo("부산광역시 해운대구");
        }
    }

    @Nested
    @DisplayName("Record 클래스 테스트")
    class RecordClassTest {

        @Test
        @DisplayName("KakaoRegionResponse record 생성")
        void shouldCreateKakaoRegionResponse() {
            // Given
            KakaoApiClient.Document document = new KakaoApiClient.Document("테스트 주소");

            // When
            KakaoApiClient.KakaoRegionResponse response =
                    new KakaoApiClient.KakaoRegionResponse(List.of(document));

            // Then
            assertThat(response.documents()).hasSize(1);
            assertThat(response.documents().get(0).address_name()).isEqualTo("테스트 주소");
        }

        @Test
        @DisplayName("Document record 생성")
        void shouldCreateDocument() {
            // When
            KakaoApiClient.Document document = new KakaoApiClient.Document("서울특별시");

            // Then
            assertThat(document.address_name()).isEqualTo("서울특별시");
        }
    }
}
