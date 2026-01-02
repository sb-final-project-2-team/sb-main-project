package com.codeit.closet.common.security.jwt;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@RequiredArgsConstructor
public class InMemoryJwtRegistry implements JwtRegistry<UUID> {

  private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
  private final Set<String> accessTokenIndexes = ConcurrentHashMap.newKeySet();
  private final Set<String> refreshTokenIndexes = ConcurrentHashMap.newKeySet();

  private final int maxActiveJwtCount;
  private final JwtTokenProvider jwtTokenProvider;

  @Override
  public void registerJwtInformation(JwtInformation jwtInformation) {
    origin.compute(jwtInformation.getUserDTO().id(), (key, queue) -> {
      if (queue == null) {
        queue = new ConcurrentLinkedQueue<>();
      }

      // maxActive 기준으로 토큰 넘어가면 제거
      if (queue.size() >= maxActiveJwtCount) {
        JwtInformation deprecatedJwtInformation = queue.poll();
        if (deprecatedJwtInformation != null) {
          log.info("Removing old token due to maxActiveJwtCount");
          removeTokenIndex(
              deprecatedJwtInformation.getAccessToken(),
              deprecatedJwtInformation.getRefreshToken()
          );
        }
      }

      // 기본적으로 새 토큰 추가
      queue.add(jwtInformation);
      addTokenIndex(
          jwtInformation.getAccessToken(),
          jwtInformation.getRefreshToken()
      );
      log.info("Total users in registry: {}", origin.size());
      log.info("Total refresh tokens: {}", refreshTokenIndexes.size());
      return queue;
    });
  }

  @Override
  public void invalidateJwtInformationByUserId(UUID userId) {
    origin.computeIfPresent(userId, (key, queue) -> {
      queue.forEach(jwtInformation -> {
        removeTokenIndex(
            jwtInformation.getAccessToken(),
            jwtInformation.getRefreshToken()
        );
      });
      queue.clear();
      return null;
    });
  }

  @Override
  public boolean hasActiveJwtInformationByUserId(UUID userId) {
    return origin.containsKey(userId);
  }

  @Override
  public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
    return accessTokenIndexes.contains(accessToken);
  }

  @Override
  public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
    return refreshTokenIndexes.contains(refreshToken);
  }

  @Override
  public void rotateJwtInformation(String refreshToken, JwtInformation newJwtInformation) {
    origin.computeIfPresent(newJwtInformation.getUserDTO().id(), (key, queue) -> {
      queue.stream().filter(jwtInformation -> jwtInformation.getRefreshToken().equals(refreshToken))
          .findFirst()
          .ifPresent(jwtInformation -> {
            removeTokenIndex(jwtInformation.getAccessToken(), jwtInformation.getRefreshToken());
            jwtInformation.rotate(
                newJwtInformation.getAccessToken(),
                newJwtInformation.getRefreshToken()
            );
            addTokenIndex(
                newJwtInformation.getAccessToken(),
                newJwtInformation.getRefreshToken()
            );
          });
      log.info("rotate Total users in registry: {}", origin.size());
      log.info("rotate Total refresh tokens: {}", refreshTokenIndexes.size());
      return queue;
    });
  }

  @Scheduled(fixedDelay = 1000 * 60 * 5)
  @Override
  public void clearExpiredJwtInformation() {
    origin.entrySet().removeIf(entry -> {
      Queue<JwtInformation> queue = entry.getValue();
      queue.removeIf(jwtInformation -> {
        boolean isExpired =
            !jwtTokenProvider.validateAccessToken(jwtInformation.getAccessToken()) ||
                !jwtTokenProvider.validateRefreshToken(jwtInformation.getRefreshToken());
        if (isExpired) {
          removeTokenIndex(
              jwtInformation.getAccessToken(),
              jwtInformation.getRefreshToken()
          );
        }
        return isExpired;
      });
      return queue.isEmpty();
    });
  }

  // 토큰 추가하기
  private void addTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.add(accessToken);
    refreshTokenIndexes.add(refreshToken);
  }

  // 토큰 제거하기
  private void removeTokenIndex(String accessToken, String refreshToken) {
    accessTokenIndexes.remove(accessToken);
    refreshTokenIndexes.remove(refreshToken);
  }
}
