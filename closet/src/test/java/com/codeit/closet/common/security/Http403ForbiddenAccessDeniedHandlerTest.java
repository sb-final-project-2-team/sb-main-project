package com.codeit.closet.common.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.codeit.closet.common.exception.ErrorResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;

class Http403ForbiddenAccessDeniedHandlerTest {

    ObjectMapper objectMapper;
    Http403ForbiddenAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new Http403ForbiddenAccessDeniedHandler(objectMapper);
    }

    @Test
    @DisplayName("권한이 없는 요청 시 403 JSON 응답을 반환한다")
    void access_denied_returns_403_json() throws Exception {
        // given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AccessDeniedException exception =
            new AccessDeniedException("접근 권한이 없습니다");

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(writer);

        // when
        handler.handle(request, response, exception);

        // then
        verify(response).setStatus(HttpServletResponse.SC_FORBIDDEN);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);

        String responseBody = stringWriter.toString();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        assertThat(jsonNode.get("status").asInt())
            .isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(jsonNode.get("message").asText())
            .contains("접근 권한이 없습니다");
    }
}
