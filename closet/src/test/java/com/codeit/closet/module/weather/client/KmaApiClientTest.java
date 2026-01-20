package com.codeit.closet.module.weather.client;

import com.codeit.closet.module.weather.config.WeatherApiProperties;
import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("KmaApiClient 테스트")
class KmaApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private WeatherApiProperties properties;

    private KmaApiClient kmaApiClient;

    @BeforeEach
    void setUp() {
        kmaApiClient = new KmaApiClient(restTemplate, properties);
    }

    private KmaApiResponse createMockResponse() {
        KmaApiResponse response = new KmaApiResponse();
        KmaApiResponse.Response resp = new KmaApiResponse.Response();
        KmaApiResponse.Header header = new KmaApiResponse.Header();
        header.setResultCode("00");
        header.setResultMsg("NORMAL_SERVICE");
        resp.setHeader(header);

        KmaApiResponse.Body body = new KmaApiResponse.Body();
        body.setDataType("JSON");
        body.setPageNo(1);
        body.setNumOfRows(100);
        body.setTotalCount(10);

        KmaApiResponse.Items items = new KmaApiResponse.Items();
        KmaApiResponse.Item item = new KmaApiResponse.Item();
        item.setBaseDate("20260120");
        item.setBaseTime("1700");
        item.setCategory("T1H");
        item.setNx(60);
        item.setNy(127);
        item.setObsrValue("15.0");
        items.setItem(List.of(item));
        body.setItems(items);

        resp.setBody(body);
        response.setResponse(resp);

        return response;
    }

    @Nested
    @DisplayName("getUltraSrtNcst - 초단기실황조회")
    class GetUltraSrtNcstTest {

        @Test
        @DisplayName("정상 호출 시 API 응답 반환")
        void shouldReturnApiResponse() {
            // Given
            Integer nx = 60;
            Integer ny = 127;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getUltraSrtNcstUrl()).willReturn("http://api.test.com/getUltraSrtNcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            KmaApiResponse result = kmaApiClient.getUltraSrtNcst(nx, ny);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getResponse().getHeader().getResultCode()).isEqualTo("00");
        }

        @Test
        @DisplayName("API 호출 시 올바른 파라미터 사용")
        void shouldUseCorrectParameters() {
            // Given
            Integer nx = 60;
            Integer ny = 127;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getUltraSrtNcstUrl()).willReturn("http://api.test.com/getUltraSrtNcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            kmaApiClient.getUltraSrtNcst(nx, ny);

            // Then
            ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
            verify(restTemplate).getForObject(uriCaptor.capture(), eq(KmaApiResponse.class));

            URI capturedUri = uriCaptor.getValue();
            String uriString = capturedUri.toString();
            assertThat(uriString).contains("nx=" + nx);
            assertThat(uriString).contains("ny=" + ny);
            assertThat(uriString).contains("dataType=JSON");
        }
    }

    @Nested
    @DisplayName("getUltraSrtFcst - 초단기예보조회")
    class GetUltraSrtFcstTest {

        @Test
        @DisplayName("정상 호출 시 API 응답 반환")
        void shouldReturnApiResponse() {
            // Given
            Integer nx = 60;
            Integer ny = 127;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getUltraSrtFcstUrl()).willReturn("http://api.test.com/getUltraSrtFcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            KmaApiResponse result = kmaApiClient.getUltraSrtFcst(nx, ny);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getResponse().getHeader().getResultCode()).isEqualTo("00");
        }

        @Test
        @DisplayName("API 호출 시 올바른 파라미터 사용")
        void shouldUseCorrectParameters() {
            // Given
            Integer nx = 55;
            Integer ny = 130;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getUltraSrtFcstUrl()).willReturn("http://api.test.com/getUltraSrtFcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            kmaApiClient.getUltraSrtFcst(nx, ny);

            // Then
            ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
            verify(restTemplate).getForObject(uriCaptor.capture(), eq(KmaApiResponse.class));

            URI capturedUri = uriCaptor.getValue();
            String uriString = capturedUri.toString();
            assertThat(uriString).contains("nx=" + nx);
            assertThat(uriString).contains("ny=" + ny);
        }
    }

    @Nested
    @DisplayName("getVilageFcst - 단기예보조회")
    class GetVilageFcstTest {

        @Test
        @DisplayName("정상 호출 시 API 응답 반환")
        void shouldReturnApiResponse() {
            // Given
            Integer nx = 60;
            Integer ny = 127;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getVilageFcstUrl()).willReturn("http://api.test.com/getVilageFcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            KmaApiResponse result = kmaApiClient.getVilageFcst(nx, ny);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getResponse().getHeader().getResultCode()).isEqualTo("00");
        }

        @Test
        @DisplayName("API 호출 시 numOfRows가 1000으로 설정됨")
        void shouldUse1000ForNumOfRows() {
            // Given
            Integer nx = 60;
            Integer ny = 127;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getVilageFcstUrl()).willReturn("http://api.test.com/getVilageFcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            kmaApiClient.getVilageFcst(nx, ny);

            // Then
            ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
            verify(restTemplate).getForObject(uriCaptor.capture(), eq(KmaApiResponse.class));

            URI capturedUri = uriCaptor.getValue();
            String uriString = capturedUri.toString();
            assertThat(uriString).contains("numOfRows=1000");
        }

        @Test
        @DisplayName("다양한 좌표로 호출 시 정상 동작")
        void shouldWorkWithVariousCoordinates() {
            // Given
            Integer nx = 90;
            Integer ny = 100;
            KmaApiResponse mockResponse = createMockResponse();

            given(properties.getServiceKey()).willReturn("test-service-key");
            given(properties.getVilageFcstUrl()).willReturn("http://api.test.com/getVilageFcst");
            given(properties.getDataType()).willReturn("JSON");
            given(restTemplate.getForObject(any(URI.class), eq(KmaApiResponse.class)))
                    .willReturn(mockResponse);

            // When
            KmaApiResponse result = kmaApiClient.getVilageFcst(nx, ny);

            // Then
            assertThat(result).isNotNull();

            ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
            verify(restTemplate).getForObject(uriCaptor.capture(), eq(KmaApiResponse.class));

            String uriString = uriCaptor.getValue().toString();
            assertThat(uriString).contains("nx=90");
            assertThat(uriString).contains("ny=100");
        }
    }
}
