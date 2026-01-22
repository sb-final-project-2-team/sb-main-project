package com.codeit.closet.module.weather.mapper;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.*;
import com.codeit.closet.module.weather.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Instant;
import java.util.UUID;

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

    @Nested
    @DisplayName("toWeatherDTO 테스트")
    class ToWeatherDTOTest {

        @Test
        @DisplayName("WeatherData → WeatherDTO 정상 매핑")
        void shouldMapWeatherDataToWeatherDTO() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시,종로구,청운동")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .temperatureCurrent(15.0)
                    .temperatureCompPrevDay(2.0)
                    .temperatureMin(10.0)
                    .temperatureMax(20.0)
                    .precipitationType(PrecipitationType.NONE)
                    .precipitationAmount(0.0)
                    .precipitationProb(10.0)
                    .humidityCurrent(60.0)
                    .humidityComparedToDayBefore(5.0)
                    .windSpeed(3.5)
                    .windAsWord(WindStrength.WEAK)
                    .build();

            // When
            WeatherDTO dto = weatherMapper.toWeatherDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.id()).isEqualTo(data.getId());
            assertThat(dto.skyStatus()).isEqualTo(SkyStatus.CLEAR);
            assertThat(dto.location()).isNotNull();
            assertThat(dto.temperature()).isNotNull();
            assertThat(dto.temperature().current()).isEqualTo(15.0);
            assertThat(dto.precipitation()).isNotNull();
            assertThat(dto.humidity()).isNotNull();
            assertThat(dto.windSpeed()).isNotNull();
        }
    }

    @Nested
    @DisplayName("toTemperatureDTO 테스트")
    class ToTemperatureDTOTest {

        @Test
        @DisplayName("WeatherData → TemperatureDTO 정상 매핑")
        void shouldMapToTemperatureDTO() {
            // Given
            WeatherData data = createTestWeatherData();

            // When
            TemperatureDTO dto = weatherMapper.toTemperatureDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.current()).isEqualTo(15.0);
            assertThat(dto.comparedToDayBefore()).isEqualTo(2.0);
            assertThat(dto.min()).isEqualTo(10.0);
            assertThat(dto.max()).isEqualTo(20.0);
        }
    }

    @Nested
    @DisplayName("toPrecipitationDTO 테스트")
    class ToPrecipitationDTOTest {

        @Test
        @DisplayName("WeatherData → PrecipitationDTO 정상 매핑")
        void shouldMapToPrecipitationDTO() {
            // Given
            WeatherData data = createTestWeatherData();

            // When
            PrecipitationDTO dto = weatherMapper.toPrecipitationDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.type()).isEqualTo(PrecipitationType.NONE);
            assertThat(dto.amount()).isEqualTo(0.0);
            assertThat(dto.probability()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("toHumidityDTO 테스트")
    class ToHumidityDTOTest {

        @Test
        @DisplayName("WeatherData → HumidityDTO 정상 매핑")
        void shouldMapToHumidityDTO() {
            // Given
            WeatherData data = createTestWeatherData();

            // When
            HumidityDTO dto = weatherMapper.toHumidityDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.current()).isEqualTo(60.0);
            assertThat(dto.comparedToDayBefore()).isEqualTo(5.0);
        }
    }

    @Nested
    @DisplayName("toWindSpeedDTO 테스트")
    class ToWindSpeedDTOTest {

        @Test
        @DisplayName("WeatherData → WindSpeedDTO 정상 매핑")
        void shouldMapToWindSpeedDTO() {
            // Given
            WeatherData data = createTestWeatherData();

            // When
            WindSpeedDTO dto = weatherMapper.toWindSpeedDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.speed()).isEqualTo(3.5);
            assertThat(dto.asWord()).isEqualTo(WindStrength.WEAK);
        }
    }

    @Nested
    @DisplayName("toWeatherSummaryDTO 테스트")
    class ToWeatherSummaryDTOTest {

        @Test
        @DisplayName("WeatherData → WeatherSummaryDTO 정상 매핑")
        void shouldMapToWeatherSummaryDTO() {
            // Given
            WeatherData data = createTestWeatherData();

            // When
            WeatherSummaryDTO dto = weatherMapper.toWeatherSummaryDTO(data);

            // Then
            assertThat(dto).isNotNull();
            assertThat(dto.weatherId()).isEqualTo(data.getId());
            assertThat(dto.temperature()).isNotNull();
            assertThat(dto.precipitation()).isNotNull();
        }
    }

    private WeatherData createTestWeatherData() {
        WeatherRegion region = WeatherRegion.builder()
                .id(UUID.randomUUID())
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,종로구,청운동")
                .build();

        Instant now = Instant.now();
        return WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(region)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)
                .temperatureCompPrevDay(2.0)
                .temperatureMin(10.0)
                .temperatureMax(20.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(10.0)
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(5.0)
                .windSpeed(3.5)
                .windAsWord(WindStrength.WEAK)
                .build();
    }


    @Nested
    @DisplayName("null 입력 처리 테스트")
    class NullInputTest {

        @Test
        @DisplayName("null WeatherData 입력 시 null 반환")
        void toWeatherDTO_shouldReturnNullForNullInput() {
            // When
            WeatherDTO result = weatherMapper.toWeatherDTO(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherRegion 입력 시 null 반환")
        void toWeatherAPILocation_shouldReturnNullForNullInput() {
            // When
            WeatherAPILocation result = weatherMapper.toWeatherAPILocation(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherData → TemperatureDTO 시 null 반환")
        void toTemperatureDTO_shouldReturnNullForNullInput() {
            // When
            TemperatureDTO result = weatherMapper.toTemperatureDTO(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherData → PrecipitationDTO 시 null 반환")
        void toPrecipitationDTO_shouldReturnNullForNullInput() {
            // When
            PrecipitationDTO result = weatherMapper.toPrecipitationDTO(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherData → HumidityDTO 시 null 반환")
        void toHumidityDTO_shouldReturnNullForNullInput() {
            // When
            HumidityDTO result = weatherMapper.toHumidityDTO(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherData → WindSpeedDTO 시 null 반환")
        void toWindSpeedDTO_shouldReturnNullForNullInput() {
            // When
            WindSpeedDTO result = weatherMapper.toWindSpeedDTO(null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("null WeatherData → WeatherSummaryDTO 시 null 반환")
        void toWeatherSummaryDTO_shouldReturnNullForNullInput() {
            // When
            WeatherSummaryDTO result = weatherMapper.toWeatherSummaryDTO(null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("null 필드 포함 데이터 처리 테스트")
    class NullFieldTest {

        @Test
        @DisplayName("weatherRegion이 null인 WeatherData 처리")
        void toWeatherDTO_shouldHandleNullWeatherRegion() {
            // Given
            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(null)  // null WeatherRegion
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .temperatureCurrent(15.0)
                    .build();

            // When
            WeatherDTO result = weatherMapper.toWeatherDTO(data);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.location()).isNull();
        }

        @Test
        @DisplayName("locationNames가 null인 WeatherRegion 처리 - 빈 리스트 반환")
        void toWeatherAPILocation_shouldHandleNullLocationNames() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames(null)  // null locationNames
                    .build();

            // When
            WeatherAPILocation result = weatherMapper.toWeatherAPILocation(region);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.locationNames()).isEmpty();  // null이 아닌 빈 리스트 반환
        }

        @Test
        @DisplayName("temperatureCompPrevDay가 null인 경우 처리")
        void toTemperatureDTO_shouldHandleNullCompPrevDay() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .temperatureCurrent(15.0)
                    .temperatureCompPrevDay(null)  // null
                    .temperatureMin(10.0)
                    .temperatureMax(20.0)
                    .build();

            // When
            TemperatureDTO result = weatherMapper.toTemperatureDTO(data);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.comparedToDayBefore()).isNull();
        }

        @Test
        @DisplayName("humidityComparedToDayBefore가 null인 경우 처리")
        void toHumidityDTO_shouldHandleNullComparedToDayBefore() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .humidityCurrent(60.0)
                    .humidityComparedToDayBefore(null)  // null - 실제로는 0.0 기본값
                    .build();

            // When
            HumidityDTO result = weatherMapper.toHumidityDTO(data);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("windAsWord가 null인 경우 처리")
        void toWindSpeedDTO_shouldHandleNullWindAsWord() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .windSpeed(5.0)
                    .windAsWord(null)  // null
                    .build();

            // When
            WindSpeedDTO result = weatherMapper.toWindSpeedDTO(data);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.asWord()).isNull();
        }

        @Test
        @DisplayName("precipitationType이 null인 경우 처리")
        void toPrecipitationDTO_shouldHandleNullPrecipitationType() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .precipitationType(null)  // null
                    .precipitationAmount(0.0)
                    .precipitationProb(0.0)
                    .build();

            // When
            PrecipitationDTO result = weatherMapper.toPrecipitationDTO(data);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.type()).isNull();
        }
    }

    @Nested
    @DisplayName("다양한 SkyStatus 매핑 테스트")
    class SkyStatusMappingTest {

        @Test
        @DisplayName("MOSTLY_CLOUDY 상태 매핑")
        void shouldMapMostlyCloudyStatus() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData mostlyCloudyData = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.MOSTLY_CLOUDY)
                    .temperatureCurrent(15.0)
                    .build();

            // When
            WeatherDTO result = weatherMapper.toWeatherDTO(mostlyCloudyData);

            // Then
            assertThat(result.skyStatus()).isEqualTo(SkyStatus.MOSTLY_CLOUDY);
        }

        @Test
        @DisplayName("CLOUDY 상태 매핑")
        void shouldMapCloudyStatus() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData cloudyData = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLOUDY)
                    .temperatureCurrent(15.0)
                    .build();

            // When
            WeatherDTO result = weatherMapper.toWeatherDTO(cloudyData);

            // Then
            assertThat(result.skyStatus()).isEqualTo(SkyStatus.CLOUDY);
        }
    }

    @Nested
    @DisplayName("다양한 WindStrength 매핑 테스트")
    class WindStrengthMappingTest {

        @Test
        @DisplayName("MODERATE 풍속 매핑")
        void shouldMapModerateWindStrength() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .windSpeed(6.0)
                    .windAsWord(WindStrength.MODERATE)
                    .build();

            // When
            WindSpeedDTO result = weatherMapper.toWindSpeedDTO(data);

            // Then
            assertThat(result.asWord()).isEqualTo(WindStrength.MODERATE);
        }

        @Test
        @DisplayName("STRONG 풍속 매핑")
        void shouldMapStrongWindStrength() {
            // Given
            WeatherRegion region = WeatherRegion.builder()
                    .id(UUID.randomUUID())
                    .x(60)
                    .y(127)
                    .latitude(37.5665)
                    .longitude(126.9780)
                    .locationNames("서울특별시")
                    .build();

            Instant now = Instant.now();
            WeatherData data = WeatherData.builder()
                    .id(UUID.randomUUID())
                    .weatherRegion(region)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .windSpeed(12.0)
                    .windAsWord(WindStrength.STRONG)
                    .build();

            // When
            WindSpeedDTO result = weatherMapper.toWindSpeedDTO(data);

            // Then
            assertThat(result.asWord()).isEqualTo(WindStrength.STRONG);
        }
    }
}
