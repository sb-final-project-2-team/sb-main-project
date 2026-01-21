package com.codeit.closet.common.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.codeit.closet.common.redis.RedisLockProvider;
import com.codeit.closet.module.user.dto.user.UserDTO;
import com.codeit.closet.module.user.entity.UserRole;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

@ExtendWith(MockitoExtension.class)
class RedisJwtRegistryTest {

  @Mock
  RedisTemplate<String, Object> redisTemplate;

  @Mock
  RedisLockProvider redisLockProvider;

  @Mock
  JwtTokenProvider jwtTokenProvider;

  @Mock
  ListOperations<String, Object> listOps;

  @Mock
  SetOperations<String, Object> setOps;

  RedisJwtRegistry registry;

  static final UUID USER_ID =
      UUID.fromString("00000000-0000-0000-0000-000000000000");

  @BeforeEach
  void setUp() {
    registry = new RedisJwtRegistry(
        2, // maxActiveJwtCount
        jwtTokenProvider,
        redisTemplate,
        redisLockProvider
    );
  }

  private UserDTO userDTO() {
    return new UserDTO(
        USER_ID,
        Instant.now(),
        "test@test.com",
        "tester",
        UserRole.USER,
        false
    );
  }

  @Test
  @DisplayName("최대 토큰 개수 초과 시 가장 오래된 JWT를 제거하고 등록한다")
  void register_jwt_eviction() {
    // given
    when(redisTemplate.opsForList()).thenReturn(listOps);
    when(redisTemplate.opsForSet()).thenReturn(setOps);

    UserDTO userDTO = userDTO();

    JwtInformation oldToken = mock(JwtInformation.class);
    JwtInformation newToken = mock(JwtInformation.class);

    when(newToken.getUserDTO()).thenReturn(userDTO);
    when(newToken.getAccessToken()).thenReturn("NEW_ACCESS");
    when(newToken.getRefreshToken()).thenReturn("NEW_REFRESH");

    when(oldToken.getAccessToken()).thenReturn("OLD_ACCESS");
    when(oldToken.getRefreshToken()).thenReturn("OLD_REFRESH");

    when(listOps.size("jwt:user:" + USER_ID)).thenReturn(2L, 1L);
    when(listOps.leftPop("jwt:user:" + USER_ID)).thenReturn(oldToken);

    // when
    registry.registerJwtInformation(newToken);

    // then
    verify(redisLockProvider).acquireLock(USER_ID.toString());
    verify(listOps).rightPush("jwt:user:" + USER_ID, newToken);
    verify(setOps).remove("jwt:access_tokens", "OLD_ACCESS");
    verify(setOps).remove("jwt:refresh_tokens", "OLD_REFRESH");
    verify(setOps).add("jwt:access_tokens", "NEW_ACCESS");
    verify(setOps).add("jwt:refresh_tokens", "NEW_REFRESH");
    verify(redisLockProvider).releaseLock(USER_ID.toString());
  }


  @Test
  @DisplayName("유저의 모든 JWT 정보를 삭제한다")
  void invalidate_jwt_by_user_id() {
    // given
    when(redisTemplate.opsForList()).thenReturn(listOps);
    when(redisTemplate.opsForSet()).thenReturn(setOps);

    JwtInformation token = mock(JwtInformation.class);
    when(token.getAccessToken()).thenReturn("ACCESS");
    when(token.getRefreshToken()).thenReturn("REFRESH");

    when(listOps.range("jwt:user:" + USER_ID, 0, -1))
        .thenReturn(List.of(token));

    // when
    registry.invalidateJwtInformationByUserId(USER_ID);

    // then
    verify(redisLockProvider).acquireLock(USER_ID.toString());
    verify(setOps).remove("jwt:access_tokens", "ACCESS");
    verify(setOps).remove("jwt:refresh_tokens", "REFRESH");
    verify(redisTemplate).delete("jwt:user:" + USER_ID);
    verify(redisLockProvider).releaseLock(USER_ID.toString());
  }

  @Test
  @DisplayName("유저에게 활성 JWT가 존재하면 true를 반환한다")
  void has_active_jwt_by_user_id() {
    // given
    when(redisTemplate.opsForList()).thenReturn(listOps);
    when(listOps.size("jwt:user:" + USER_ID)).thenReturn(1L);

    // when
    boolean result = registry.hasActiveJwtInformationByUserId(USER_ID);

    // then
    assertThat(result).isTrue();
  }


  @Test
  @DisplayName("AccessToken 인덱스에 존재하면 true를 반환한다")
  void has_active_jwt_by_access_token() {
    // given
    when(redisTemplate.opsForSet()).thenReturn(setOps);
    when(setOps.isMember("jwt:access_tokens", "ACCESS")).thenReturn(true);

    // when
    boolean result =
        registry.hasActiveJwtInformationByAccessToken("ACCESS");

    // then
    assertThat(result).isTrue();
  }


  @Test
  @DisplayName("RefreshToken 인덱스에 존재하면 true를 반환한다")
  void has_active_jwt_by_refresh_token() {
    // given
    when(redisTemplate.opsForSet()).thenReturn(setOps);
    when(setOps.isMember("jwt:refresh_tokens", "REFRESH")).thenReturn(true);

    // when
    boolean result =
        registry.hasActiveJwtInformationByRefreshToken("REFRESH");

    // then
    assertThat(result).isTrue();
  }


  @Test
  @DisplayName("RefreshToken 기준으로 JWT를 회전시킨다")
  void rotate_jwt_information() {
    // given
    when(redisTemplate.opsForList()).thenReturn(listOps);
    when(redisTemplate.opsForSet()).thenReturn(setOps);

    UserDTO userDTO = userDTO();

    JwtInformation oldJwt = mock(JwtInformation.class);
    JwtInformation newJwt = mock(JwtInformation.class);

    when(newJwt.getUserDTO()).thenReturn(userDTO);
    when(newJwt.getAccessToken()).thenReturn("NEW_ACCESS");
    when(newJwt.getRefreshToken()).thenReturn("NEW_REFRESH");

    when(oldJwt.getAccessToken()).thenReturn("OLD_ACCESS");
    when(oldJwt.getRefreshToken()).thenReturn("OLD_REFRESH");

    when(listOps.range("jwt:user:" + USER_ID, 0, -1))
        .thenReturn(List.of(oldJwt));

    // when
    registry.rotateJwtInformation("OLD_REFRESH", newJwt);

    // then
    verify(redisLockProvider).acquireLock(USER_ID.toString());
    verify(setOps).remove("jwt:access_tokens", "OLD_ACCESS");
    verify(setOps).remove("jwt:refresh_tokens", "OLD_REFRESH");
    verify(oldJwt).rotate("NEW_ACCESS", "NEW_REFRESH");
    verify(listOps).set("jwt:user:" + USER_ID, 0, oldJwt);
    verify(setOps).add("jwt:access_tokens", "NEW_ACCESS");
    verify(setOps).add("jwt:refresh_tokens", "NEW_REFRESH");
    verify(redisLockProvider).releaseLock(USER_ID.toString());
  }

  @Test
  @DisplayName("만료된 JWT는 Redis에서 제거되고 인덱스도 삭제된다")
  void clear_expired_jwt() {
    // given
    String userKey = "jwt:user:" + USER_ID;

    JwtInformation expiredJwt = mock(JwtInformation.class);
    when(expiredJwt.getAccessToken()).thenReturn("EXPIRED_ACCESS");
    when(expiredJwt.getRefreshToken()).thenReturn("EXPIRED_REFRESH");

    Cursor<String> cursor = mock(Cursor.class);

    when(redisTemplate.scan(any())).thenReturn(cursor);
    when(cursor.hasNext()).thenReturn(true, false);
    when(cursor.next()).thenReturn(userKey);

    when(redisTemplate.opsForList()).thenReturn(listOps);
    when(redisTemplate.opsForSet()).thenReturn(setOps);

    when(listOps.range(userKey, 0, -1))
        .thenReturn(List.of(expiredJwt));

    when(jwtTokenProvider.validateAccessToken("EXPIRED_ACCESS"))
        .thenReturn(false);

    // when
    registry.clearExpiredJwtInformation();

    // then
    verify(listOps).remove(userKey, 1, expiredJwt);
    verify(setOps).remove("jwt:access_tokens", "EXPIRED_ACCESS");
    verify(setOps).remove("jwt:refresh_tokens", "EXPIRED_REFRESH");
    verify(redisTemplate).delete(userKey);
  }



}