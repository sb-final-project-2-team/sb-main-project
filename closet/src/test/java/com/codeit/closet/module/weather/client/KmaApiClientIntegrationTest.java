package com.codeit.closet.module.weather.client;

import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 기상청 API 실제 호출 통합 테스트
 * 서울 종로구 좌표: nx=60, ny=127
 * KMA_SERVICE_KEY 환경변수가 설정된 경우에만 실행됩니다.
 */
@SpringBootTest
@DisplayName("기상청 API 통합 테스트")
@EnabledIfEnvironmentVariable(named = "KMA_SERVICE_KEY", matches = ".+")
class KmaApiClientIntegrationTest {

    @Autowired
    private KmaApiClient kmaApiClient;

    // 서울 종로구 격자 좌표
    private static final int NX = 60;
    private static final int NY = 127;

    @Test
    @DisplayName("초단기실황 API 호출 테스트")
    void getUltraSrtNcst_shouldReturnValidResponse() {
        // When
        KmaApiResponse response = kmaApiClient.getUltraSrtNcst(NX, NY);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getHeader()).isNotNull();

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        System.out.println("=== 초단기실황 API 응답 ===");
        System.out.println("결과코드: " + resultCode);
        System.out.println("결과메시지: " + resultMsg);

        if ("00".equals(resultCode)) {
            assertThat(response.getResponse().getBody()).isNotNull();
            assertThat(response.getResponse().getBody().getItems()).isNotNull();
            assertThat(response.getResponse().getBody().getItems().getItem()).isNotEmpty();

            System.out.println("총 데이터 수: " + response.getResponse().getBody().getTotalCount());
            System.out.println("\n--- 날씨 데이터 ---");

            response.getResponse().getBody().getItems().getItem().forEach(item ->
                System.out.printf("카테고리: %s, 관측값: %s (baseDate: %s, baseTime: %s)%n",
                        item.getCategory(),
                        item.getObsrValue(),
                        item.getBaseDate(),
                        item.getBaseTime()));
        } else {
            System.out.println("API 호출 실패: " + resultMsg);
        }

        // API 호출 성공 여부 확인 (00: 정상)
        assertThat(resultCode).isEqualTo("00");
    }

    @Test
    @DisplayName("초단기예보 API 호출 테스트")
    void getUltraSrtFcst_shouldReturnValidResponse() {
        // When
        KmaApiResponse response = kmaApiClient.getUltraSrtFcst(NX, NY);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getHeader()).isNotNull();

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        System.out.println("\n=== 초단기예보 API 응답 ===");
        System.out.println("결과코드: " + resultCode);
        System.out.println("결과메시지: " + resultMsg);

        if ("00".equals(resultCode)) {
            assertThat(response.getResponse().getBody()).isNotNull();
            assertThat(response.getResponse().getBody().getItems()).isNotNull();

            System.out.println("총 데이터 수: " + response.getResponse().getBody().getTotalCount());
            System.out.println("\n--- 예보 데이터 (처음 10개) ---");

            response.getResponse().getBody().getItems().getItem().stream()
                    .limit(10)
                    .forEach(item ->
                        System.out.printf("카테고리: %s, 예보값: %s (fcstDate: %s, fcstTime: %s)%n",
                                item.getCategory(),
                                item.getFcstValue(),
                                item.getFcstDate(),
                                item.getFcstTime()));
        } else {
            System.out.println("API 호출 실패: " + resultMsg);
        }

        assertThat(resultCode).isEqualTo("00");
    }

    @Test
    @DisplayName("단기예보 API 호출 테스트")
    void getVilageFcst_shouldReturnValidResponse() {
        // When
        KmaApiResponse response = kmaApiClient.getVilageFcst(NX, NY);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResponse()).isNotNull();
        assertThat(response.getResponse().getHeader()).isNotNull();

        String resultCode = response.getResponse().getHeader().getResultCode();
        String resultMsg = response.getResponse().getHeader().getResultMsg();

        System.out.println("\n=== 단기예보 API 응답 ===");
        System.out.println("결과코드: " + resultCode);
        System.out.println("결과메시지: " + resultMsg);

        if ("00".equals(resultCode)) {
            assertThat(response.getResponse().getBody()).isNotNull();
            assertThat(response.getResponse().getBody().getItems()).isNotNull();

            System.out.println("총 데이터 수: " + response.getResponse().getBody().getTotalCount());
            System.out.println("\n--- 예보 데이터 (처음 10개) ---");

            response.getResponse().getBody().getItems().getItem().stream()
                    .limit(10)
                    .forEach(item ->
                        System.out.printf("카테고리: %s, 예보값: %s (fcstDate: %s, fcstTime: %s)%n",
                                item.getCategory(),
                                item.getFcstValue(),
                                item.getFcstDate(),
                                item.getFcstTime()));
        } else {
            System.out.println("API 호출 실패: " + resultMsg);
        }

        assertThat(resultCode).isEqualTo("00");
    }
}
