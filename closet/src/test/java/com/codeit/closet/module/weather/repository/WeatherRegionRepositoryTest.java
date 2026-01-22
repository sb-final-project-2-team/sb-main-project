package com.codeit.closet.module.weather.repository;

import com.codeit.closet.module.weather.entity.WeatherRegion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("WeatherRegion Repository 테스트")
class WeatherRegionRepositoryTest {

    @Autowired
    private WeatherRegionRepository weatherRegionRepository;

    @Test
    @DisplayName("latitude, longitude를 포함한 WeatherRegion 저장 및 조회")
    void saveAndFindWeatherRegionWithLatitudeLongitude() {
        // Given: latitude, longitude를 포함한 WeatherRegion 생성
        WeatherRegion region = WeatherRegion.builder()
                .x(61)
                .y(126)
                .latitude(37.5665)  // 서울 위도
                .longitude(126.9780) // 서울 경도
                .locationNames("서울특별시,강남구,역삼동")
                .lastCollectedAt(Instant.now())
                .build();

        // When: 저장
        WeatherRegion saved = weatherRegionRepository.save(region);

        // Then: 저장 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLatitude()).isEqualTo(37.5665);
        assertThat(saved.getLongitude()).isEqualTo(126.9780);
        assertThat(saved.getX()).isEqualTo(61);
        assertThat(saved.getY()).isEqualTo(126);
        assertThat(saved.getLocationNames()).isEqualTo("서울특별시,강남구,역삼동");
    }

    @Test
    @DisplayName("격자 좌표(x, y)로 WeatherRegion 조회 시 latitude, longitude 포함")
    void findByXAndY_shouldIncludeLatitudeLongitude() {
        // Given: latitude, longitude를 포함한 WeatherRegion 저장
        WeatherRegion region = WeatherRegion.builder()
                .x(60)
                .y(127)
                .latitude(37.4563)
                .longitude(126.7052)
                .locationNames("서울특별시,구로구")
                .build();
        weatherRegionRepository.save(region);

        // When: x, y로 조회
        Optional<WeatherRegion> found = weatherRegionRepository.findByXAndY(60, 127);

        // Then: latitude, longitude 포함 확인
        assertThat(found).isPresent();
        assertThat(found.get().getLatitude()).isEqualTo(37.4563);
        assertThat(found.get().getLongitude()).isEqualTo(126.7052);
    }

    @Test
    @DisplayName("latitude, longitude가 NULL인 WeatherRegion 저장 가능 (하위 호환성)")
    void saveWeatherRegionWithNullLatitudeLongitude() {
        // Given: latitude, longitude가 NULL인 WeatherRegion (기존 데이터 시뮬레이션)
        WeatherRegion region = WeatherRegion.builder()
                .x(62)
                .y(125)
                .latitude(null)  // NULL 허용
                .longitude(null) // NULL 허용
                .locationNames("서울특별시,마포구")
                .build();

        // When: 저장
        WeatherRegion saved = weatherRegionRepository.save(region);

        // Then: 저장 성공, NULL 값 확인
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getLatitude()).isNull();
        assertThat(saved.getLongitude()).isNull();
        assertThat(saved.getX()).isEqualTo(62);
        assertThat(saved.getY()).isEqualTo(125);
    }

    @Test
    @DisplayName("latitude, longitude 업데이트 가능")
    void updateLatitudeLongitude() {
        // Given: latitude, longitude가 NULL인 WeatherRegion 저장
        WeatherRegion region = WeatherRegion.builder()
                .x(63)
                .y(124)
                .latitude(null)
                .longitude(null)
                .locationNames("서울특별시,용산구")
                .build();
        WeatherRegion saved = weatherRegionRepository.save(region);

        // When: latitude, longitude 업데이트 (실제로는 불변 객체이므로 새로 빌드)
        WeatherRegion updated = WeatherRegion.builder()
                .id(saved.getId())
                .x(saved.getX())
                .y(saved.getY())
                .latitude(37.5326)  // 업데이트
                .longitude(126.9905) // 업데이트
                .locationNames(saved.getLocationNames())
                .createdAt(saved.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        WeatherRegion result = weatherRegionRepository.save(updated);

        // Then: 업데이트 확인
        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getLatitude()).isEqualTo(37.5326);
        assertThat(result.getLongitude()).isEqualTo(126.9905);
    }
}
