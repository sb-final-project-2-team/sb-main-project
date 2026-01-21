package com.codeit.closet.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtAuthenticationChannelInterceptor 단위 테스트")
class JwtAuthenticationChannelInterceptorTest {

    @Mock
    JwtTokenProvider jwtTokenProvider;

    @Mock
    RoleHierarchy roleHierarchy;

    @Mock
    JwtRegistry<UUID> jwtRegistry;

    @Mock
    MessageChannel channel;

    @InjectMocks
    JwtAuthenticationChannelInterceptor interceptor;

    @Test
    @DisplayName("CONNECT 요청에서 Authorization 헤더가 없으면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_MissingAuthorizationHeader() {
        // Given: CONNECT accessor 생성
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());

        Message<byte[]> message = createMessage(accessor);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: 토큰이 없으니 tokenProvider/jwtRegistry는 호출되면 안 됨
        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(jwtRegistry);
    }

    @Test
    @DisplayName("CONNECT 요청에서 토큰이 있지만 validateAccessToken이 false면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_InvalidTokenByValidation() {
        // Given
        String rawToken = "abc.def.ghi";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);

        Message<byte[]> message = createMessage(accessor);

        when(jwtTokenProvider.validateAccessToken(rawToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: validate에서 실패했으므로 registry/parse는 호출되면 안 됨
        verify(jwtRegistry, never()).hasActiveJwtInformationByAccessToken(anyString());
        verify(jwtTokenProvider, never()).parseAccessToken(anyString());
    }

    @Test
    @DisplayName("CONNECT 요청에서 validate는 성공하지만 registry 체크가 false면 INVALID_TOKEN 예외가 발생한다")
    void preSend_Failure_InvalidTokenByRegistry() {
        // Given
        String rawToken = "abc.def.ghi";

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setSessionAttributes(new HashMap<>());
        accessor.addNativeHeader(HttpHeaders.AUTHORIZATION, "Bearer " + rawToken);

        Message<byte[]> message = createMessage(accessor);

        when(jwtTokenProvider.validateAccessToken(rawToken)).thenReturn(true);
        when(jwtRegistry.hasActiveJwtInformationByAccessToken(rawToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> interceptor.preSend(message, channel))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("INVALID_TOKEN");

        // Then: registry 실패니까 parse는 호출되면 안 됨
        verify(jwtTokenProvider, never()).parseAccessToken(anyString());
    }

    /**
     * STOMP accessor 기반으로 "mutable 상태"의 메시지를 만든다.
     */
    private Message<byte[]> createMessage(StompHeaderAccessor accessor) {
        accessor.setLeaveMutable(true); // ✅ 핵심: Already immutable 방지
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
