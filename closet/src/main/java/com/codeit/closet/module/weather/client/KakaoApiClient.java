package com.codeit.closet.module.weather.client;

import com.codeit.closet.module.weather.config.KakaoApiProperties;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoApiClient {

  private final RestTemplate restTemplate;
  private final KakaoApiProperties kakaoApiProperties;

  public String getRegionByCoordinate(Double longitude, Double latitude) {
    URI uri = UriComponentsBuilder
        .fromUriString(
            kakaoApiProperties.getBaseUrl() + "/v2/local/geo/coord2regioncode.json")
        .queryParam("x", longitude)
        .queryParam("y", latitude)
        .build()
        .encode()
        .toUri();

    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "KakaoAK " + kakaoApiProperties.getRestKey());

    HttpEntity<Void> entity = new HttpEntity<>(headers);

    ResponseEntity<KakaoRegionResponse> response = restTemplate.exchange(
        uri,
        HttpMethod.GET,
        entity,
        KakaoRegionResponse.class
    );

    KakaoRegionResponse body = response.getBody();
    if (body == null || body.documents().isEmpty()) {
      throw new IllegalStateException("카카오 지역 정보가 존재하지 않습니다.");
    }

    return body.documents().get(0).address_name();
  }

  public record KakaoRegionResponse(
      List<Document> documents
  ) {}

  public record Document(
      String address_name
  ) {}
}

