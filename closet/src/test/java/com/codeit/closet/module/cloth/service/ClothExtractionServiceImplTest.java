package com.codeit.closet.module.cloth.service;

import com.codeit.closet.module.cloth.dto.ClothDTO;
import com.codeit.closet.module.cloth.service.impl.ClothExtractionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
@DisplayName("ClothExtractionService 테스트")
class ClothExtractionServiceImplTest {

    private ClothExtractionServiceImpl clothExtractionService;

    @BeforeEach
    void setUp() {
        clothExtractionService = new ClothExtractionServiceImpl();
    }

    @Test
    @DisplayName("URL이 null일 때 예외 발생")
    void extract_NullUrl() {
        // given
        String url = null;

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url은 필수입니다");
    }

    @Test
    @DisplayName("URL이 빈 문자열일 때 예외 발생")
    void extract_EmptyUrl() {
        // given
        String url = "";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url은 필수입니다");
    }

    @Test
    @DisplayName("URL이 공백만 있을 때 예외 발생")
    void extract_BlankUrl() {
        // given
        String url = "   ";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url은 필수입니다");
    }

    @Test
    @DisplayName("잘못된 URL 형식일 때 예외 발생")
    void extract_InvalidUrlFormat() {
        // given
        String url = "not a valid url";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바르지 않은 URL 형식입니다");
    }

    @Test
    @DisplayName("http/https가 아닌 프로토콜일 때 예외 발생")
    void extract_InvalidProtocol() {
        // given
        String url = "ftp://example.com";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바르지 않은 URL 형식입니다");
    }

    @Test
    @DisplayName("프로토콜이 없을 때 예외 발생")
    void extract_NoProtocol() {
        // given
        String url = "www.musinsa.com/product/123";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바르지 않은 URL 형식입니다");
    }

    @Test
    @DisplayName("지원하지 않는 도메인일 때 예외 발생")
    void extract_UnsupportedDomain() {
        // given
        String url = "https://www.naver.com";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 사이트입니다");
    }

    @Test
    @DisplayName("무신사 도메인 지원 확인")
    void extract_MusinsaDomain() {
        // given
        String url = "https://www.musinsa.com/product/99999999";

        // when & then
        // 도메인 검증은 통과하고, 존재하지 않는 상품 페이지 접근 시 실패할 수 있음
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("지그재그 도메인 지원 확인")
    void extract_ZigzagDomain() {
        // given
        String url = "https://zigzag.kr/product/99999999";

        // when & then
        // 도메인 검증은 통과하고, 존재하지 않는 상품 페이지 접근 시 실패할 수 있음
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("29CM 도메인 지원 확인")
    void extract_29cmDomain() {
        // given
        String url = "https://29cm.co.kr/product/99999999";

        // when & then
        // 도메인 검증은 통과하고, 존재하지 않는 상품 페이지 접근 시 실패할 수 있음
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("서브도메인이 있는 무신사 URL - 도메인 검증 통과 확인")
    void extract_MusinsaSubdomain() {
        // given
        String url = "https://notexist.musinsa.com/product/123";

        // when & then
        // 도메인 검증은 통과하고, 실제 접근 시 실패해야 함
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("서브도메인이 있는 지그재그 URL - 도메인 검증 통과 확인")
    void extract_ZigzagSubdomain() {
        // given
        String url = "https://notexist.zigzag.kr/product/123";

        // when & then
        // 도메인 검증은 통과하고, 실제 접근 시 실패해야 함
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("서브도메인이 있는 29CM URL - 도메인 검증 통과 확인")
    void extract_29cmSubdomain() {
        // given
        String url = "https://notexist.29cm.co.kr/product/123";

        // when & then
        // 도메인 검증은 통과하고, 실제 접근 시 실패해야 함
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("대문자 프로토콜도 허용")
    void extract_UppercaseProtocol() {
        // given
        String url = "HTTPS://www.musinsa.com/product/99999999";

        // when & then
        // 대소문자 구분 없이 프로토콜 허용
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("URL 앞뒤 공백 제거")
    void extract_TrimUrl() {
        // given
        String url = "  https://www.musinsa.com/product/99999999  ";

        // when & then
        // 공백이 제거되고 정상 처리됨
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }

    @Test
    @DisplayName("호스트가 null일 때 예외 발생")
    void extract_NullHost() {
        // given
        String url = "https://";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("올바르지 않은 URL 형식입니다");
    }

    @Test
    @DisplayName("지원하지 않는 다른 쇼핑몰 도메인")
    void extract_OtherShoppingMall() {
        // given
        String url = "https://www.coupang.com/product/123";

        // when & then
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 사이트입니다");
    }

    @Test
    @DisplayName("HTTP 프로토콜도 허용")
    void extract_HttpProtocol() {
        // given
        String url = "http://www.musinsa.com/product/99999999";

        // when & then
        // HTTP 프로토콜도 허용됨
        assertThatThrownBy(() -> clothExtractionService.extract(url))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("URL 접근/파싱 실패");
    }
}
