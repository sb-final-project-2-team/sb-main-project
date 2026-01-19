package com.codeit.closet.module.weather.mapper;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("WeatherMapper 테스트")
class WeatherMapperTest {

    @Autowired
    private WeatherMapper weatherMapper;

    @Test
    @DisplayName("WeatherRegion → WeatherAPILocation 매핑 시 latitude, longitude 포함")
    void toWeatherAPILocation_shouldMapLatitudeLongitudeFromEntity() {
        // Given: latitude, longitude를 포함한 WeatherRegion
        WeatherRegion region = WeatherRegion.builder()
                .x(61)
                .y(126)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,강남구,역삼동")
                .build();

        // When: WeatherAPILocation으로 변환
        WeatherAPILocation location = weatherMapper.toWeatherAPILocation(region);

        // Then: latitude, longitude가 엔티티 값으로 매핑됨
        assertThat(location).isNotNull();
        assertThat(location.latitude()).isEqualTo(37.5665);
        assertThat(location.longitude()).isEqualTo(126.9780);
        assertThat(location.x()).isEqualTo(61);
        assertThat(location.y()).isEqualTo(126);
        assertThat(location.locationNames()).containsExactly("서울특별시", "강남구", "역삼동");
    }

    @Test
    @DisplayName("latitude, longitude가 NULL인 경우에도 매핑 가능")
    void toWeatherAPILocation_shouldHandleNullLatitudeLongitude() {
        // Given: latitude, longitude가 NULL인 WeatherRegion
        WeatherRegion region = WeatherRegion.builder()
                .x(62)
                .y(125)
                .latitude(null)
                .longitude(null)
                .locationNames("서울특별시,마포구")
                .build();

        // When: WeatherAPILocation으로 변환
        WeatherAPILocation location = weatherMapper.toWeatherAPILocation(region);

        // Then: NULL 값도 정상 매핑됨
        assertThat(location).isNotNull();
        assertThat(location.latitude()).isNull();
        assertThat(location.longitude()).isNull();
        assertThat(location.x()).isEqualTo(62);
        assertThat(location.y()).isEqualTo(125);
    }

    @Test
    @DisplayName("locationNames 문자열을 List로 정상 변환")
    void toWeatherAPILocation_shouldSplitLocationNamesToList() {
        // Given: 쉼표로 구분된 locationNames
        WeatherRegion region = WeatherRegion.builder()
                .x(63)
                .y(124)
                .latitude(37.5326)
                .longitude(126.9905)
                .locationNames("서울특별시,용산구,이태원동")
                .build();

        // When: WeatherAPILocation으로 변환
        WeatherAPILocation location = weatherMapper.toWeatherAPILocation(region);

        // Then: List로 정상 분리됨
        assertThat(location.locationNames())
                .hasSize(3)
                .containsExactly("서울특별시", "용산구", "이태원동");
    }

    @Test
    @DisplayName("빈 locationNames 처리")
    void toWeatherAPILocation_shouldHandleEmptyLocationNames() {
        // Given: 빈 locationNames
        WeatherRegion region = WeatherRegion.builder()
                .x(64)
                .y(123)
                .latitude(37.5172)
                .longitude(127.0473)
                .locationNames("")
                .build();

        // When: WeatherAPILocation으로 변환
        WeatherAPILocation location = weatherMapper.toWeatherAPILocation(region);

        // Then: 빈 List 반환
        assertThat(location.locationNames()).isEmpty();
    }
}
