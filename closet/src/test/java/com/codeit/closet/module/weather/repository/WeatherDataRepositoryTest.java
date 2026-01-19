package com.codeit.closet.module.weather.repository;

import com.codeit.closet.module.weather.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
@DisplayName("WeatherDataRepository 테스트")
class WeatherDataRepositoryTest {

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private WeatherRegionRepository weatherRegionRepository;

    private WeatherRegion testRegion;
    private Instant now;

    @BeforeEach
    void setUp() {
        now = Instant.now();

        // 테스트용 WeatherRegion 생성
        testRegion = WeatherRegion.builder()
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,종로구,청운동")
                .build();
        testRegion = weatherRegionRepository.save(testRegion);
    }

    @Nested
    @DisplayName("findClosestByWeatherRegionIdAndTime 테스트 - 어제 대비 비교 기능")
    class FindClosestByWeatherRegionIdAndTimeTest {

        @Test
        @DisplayName("어제 시간대의 SHORT_FCST 데이터가 있으면 정상 조회됨")
        void findClosestByWeatherRegionIdAndTime_withYesterdayData_returnsData() {
            // Given: 어제 시간대의 SHORT_FCST 데이터 저장
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);

            WeatherData yesterdayData = WeatherData.builder()
                    .weatherRegion(testRegion)
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(yesterday)
                    .forecastedAt(yesterday.minus(3, ChronoUnit.HOURS))
                    .skyStatus(SkyStatus.CLOUDY)
                    .temperatureCurrent(10.0)
                    .temperatureMin(8.0)
                    .temperatureMax(15.0)
                    .precipitationType(PrecipitationType.NONE)
                    .precipitationAmount(0.0)
                    .precipitationProb(20.0)
                    .humidityCurrent(50.0)
                    .humidityComparedToDayBefore(0.0)
                    .windSpeed(3.0)
                    .windAsWord(WindStrength.WEAK)
                    .build();
            weatherDataRepository.save(yesterdayData);

            // When: 어제 시간대 ±3시간 범위로 조회
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 어제 데이터 조회 성공
            assertThat(result).isPresent();
            assertThat(result.get().getTemperatureCurrent()).isEqualTo(10.0);
            assertThat(result.get().getHumidityCurrent()).isEqualTo(50.0);
            assertThat(result.get().getForecastKind()).isEqualTo(ForecastKind.SHORT_FCST);
        }

        @Test
        @DisplayName("어제 시간대에 가장 가까운 데이터를 반환함")
        void findClosestByWeatherRegionIdAndTime_returnsClosestData() {
            // Given: 어제 시간대 주변에 여러 데이터 저장
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);

            // 어제보다 2시간 전 데이터
            WeatherData data2HoursBefore = createShortFcstData(yesterday.minus(2, ChronoUnit.HOURS), 8.0, 40.0);
            // 어제 정확한 시간 데이터
            WeatherData dataExact = createShortFcstData(yesterday, 10.0, 50.0);
            // 어제보다 2시간 후 데이터
            WeatherData data2HoursAfter = createShortFcstData(yesterday.plus(2, ChronoUnit.HOURS), 12.0, 55.0);

            weatherDataRepository.saveAll(List.of(data2HoursBefore, dataExact, data2HoursAfter));

            // When: 어제 시간대 ±3시간 범위로 조회
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 가장 가까운 시간(정확히 어제)의 데이터 반환
            assertThat(result).isPresent();
            assertThat(result.get().getTemperatureCurrent()).isEqualTo(10.0);
            assertThat(result.get().getHumidityCurrent()).isEqualTo(50.0);
        }

        @Test
        @DisplayName("어제 시간대에 데이터가 없으면 빈 결과 반환")
        void findClosestByWeatherRegionIdAndTime_withoutYesterdayData_returnsEmpty() {
            // Given: 오늘 데이터만 저장 (어제 데이터 없음)
            WeatherData todayData = createShortFcstData(now, 15.0, 60.0);
            weatherDataRepository.save(todayData);

            // When: 어제 시간대 ±3시간 범위로 조회
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 빈 결과
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("ULTRA_NOW 데이터는 SHORT_FCST 조회 시 제외됨")
        void findClosestByWeatherRegionIdAndTime_excludesUltraNow() {
            // Given: 어제 시간대에 ULTRA_NOW 데이터만 저장
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);

            WeatherData ultraNowData = WeatherData.builder()
                    .weatherRegion(testRegion)
                    .forecastKind(ForecastKind.ULTRA_NOW)  // ULTRA_NOW
                    .forecastAt(yesterday)
                    .forecastedAt(yesterday)
                    .skyStatus(SkyStatus.CLEAR)
                    .temperatureCurrent(11.0)
                    .temperatureMin(9.0)
                    .temperatureMax(16.0)
                    .precipitationType(PrecipitationType.NONE)
                    .precipitationAmount(0.0)
                    .precipitationProb(0.0)
                    .humidityCurrent(45.0)
                    .humidityComparedToDayBefore(0.0)
                    .windSpeed(2.0)
                    .windAsWord(WindStrength.WEAK)
                    .build();
            weatherDataRepository.save(ultraNowData);

            // When: SHORT_FCST로 조회
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",  // SHORT_FCST만 조회
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: ULTRA_NOW 데이터는 조회되지 않음
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("다른 지역의 데이터는 조회되지 않음")
        void findClosestByWeatherRegionIdAndTime_excludesOtherRegion() {
            // Given: 다른 지역 생성 및 데이터 저장
            WeatherRegion otherRegion = WeatherRegion.builder()
                    .x(61)
                    .y(128)
                    .latitude(37.4563)
                    .longitude(126.7052)
                    .locationNames("서울특별시,구로구")
                    .build();
            otherRegion = weatherRegionRepository.save(otherRegion);

            Instant yesterday = now.minus(24, ChronoUnit.HOURS);

            // 다른 지역에 어제 데이터 저장
            WeatherData otherRegionData = WeatherData.builder()
                    .weatherRegion(otherRegion)  // 다른 지역
                    .forecastKind(ForecastKind.SHORT_FCST)
                    .forecastAt(yesterday)
                    .forecastedAt(yesterday)
                    .skyStatus(SkyStatus.CLOUDY)
                    .temperatureCurrent(9.0)
                    .temperatureMin(7.0)
                    .temperatureMax(14.0)
                    .precipitationType(PrecipitationType.NONE)
                    .precipitationAmount(0.0)
                    .precipitationProb(30.0)
                    .humidityCurrent(55.0)
                    .humidityComparedToDayBefore(0.0)
                    .windSpeed(4.0)
                    .windAsWord(WindStrength.MODERATE)
                    .build();
            weatherDataRepository.save(otherRegionData);

            // When: 테스트 지역(testRegion)으로 조회
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),  // testRegion으로 조회
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 다른 지역 데이터는 조회되지 않음
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("어제 대비 온도/습도 비교 시나리오 테스트")
    class ComparedToDayBeforeScenarioTest {

        @Test
        @DisplayName("어제 대비 온도/습도 차이 계산이 가능한 데이터 구조 검증")
        void yesterdayComparison_dataStructureVerification() {
            // Given: 어제 단기예보 데이터 (배치에서 수집된 데이터 시뮬레이션)
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);
            WeatherData yesterdayForecast = createShortFcstData(yesterday, 10.0, 50.0);
            weatherDataRepository.save(yesterdayForecast);

            // 오늘 초단기실황 데이터
            WeatherData todayUltraNow = WeatherData.builder()
                    .weatherRegion(testRegion)
                    .forecastKind(ForecastKind.ULTRA_NOW)
                    .forecastAt(now)
                    .forecastedAt(now)
                    .skyStatus(SkyStatus.CLEAR)
                    .temperatureCurrent(15.0)  // 어제보다 +5도
                    .temperatureMin(12.0)
                    .temperatureMax(18.0)
                    .precipitationType(PrecipitationType.NONE)
                    .precipitationAmount(0.0)
                    .precipitationProb(0.0)
                    .humidityCurrent(60.0)  // 어제보다 +10%
                    .humidityComparedToDayBefore(0.0)
                    .windSpeed(2.5)
                    .windAsWord(WindStrength.WEAK)
                    .build();
            weatherDataRepository.save(todayUltraNow);

            // When: 어제 단기예보 데이터 조회
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> yesterdayResult = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 어제 대비 계산 가능 검증
            assertThat(yesterdayResult).isPresent();

            double tempDiff = todayUltraNow.getTemperatureCurrent() - yesterdayResult.get().getTemperatureCurrent();
            double humidityDiff = todayUltraNow.getHumidityCurrent() - yesterdayResult.get().getHumidityCurrent();

            assertThat(tempDiff).isEqualTo(5.0);   // 15.0 - 10.0 = 5.0
            assertThat(humidityDiff).isEqualTo(10.0);  // 60.0 - 50.0 = 10.0
        }

        @Test
        @DisplayName("3시간 간격 단기예보에서 가장 가까운 시간대 데이터 조회")
        void yesterdayComparison_closestTimeInThreeHourInterval() {
            // Given: 3시간 간격 단기예보 데이터 (실제 기상청 API 패턴)
            Instant yesterday = now.minus(24, ChronoUnit.HOURS);

            // 어제 06시 데이터
            WeatherData data06 = createShortFcstData(yesterday.minus(3, ChronoUnit.HOURS), 8.0, 45.0);
            // 어제 09시 데이터 (target: 24시간 전)
            WeatherData data09 = createShortFcstData(yesterday, 10.0, 50.0);
            // 어제 12시 데이터
            WeatherData data12 = createShortFcstData(yesterday.plus(3, ChronoUnit.HOURS), 14.0, 55.0);

            weatherDataRepository.saveAll(List.of(data06, data09, data12));

            // When: 어제 시간대 조회 (target: 정확히 24시간 전)
            Instant startTime = yesterday.minus(3, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(3, ChronoUnit.HOURS);

            Optional<WeatherData> result = weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    testRegion.getId(),
                    "SHORT_FCST",
                    yesterday,
                    startTime,
                    endTime
            );

            // Then: 가장 가까운 09시(yesterday) 데이터 반환
            assertThat(result).isPresent();
            assertThat(result.get().getTemperatureCurrent()).isEqualTo(10.0);
        }
    }

    @Nested
    @DisplayName("findLatestByWeatherRegionId 테스트")
    class FindLatestByWeatherRegionIdTest {

        @Test
        @DisplayName("가장 최근에 수집된 날씨 데이터 조회")
        void findLatestByWeatherRegionId_returnsLatestData() {
            // Given: 여러 시간대 데이터 저장
            WeatherData oldData = createShortFcstData(now.minus(6, ChronoUnit.HOURS), 12.0, 50.0);
            WeatherData latestData = createShortFcstData(now, 15.0, 55.0);

            weatherDataRepository.saveAll(List.of(oldData, latestData));

            // When
            Optional<WeatherData> result = weatherDataRepository.findLatestByWeatherRegionId(testRegion.getId());

            // Then
            assertThat(result).isPresent();
            assertThat(result.get().getTemperatureCurrent()).isEqualTo(15.0);
        }

        @Test
        @DisplayName("데이터가 없으면 빈 결과 반환")
        void findLatestByWeatherRegionId_withNoData_returnsEmpty() {
            // When
            Optional<WeatherData> result = weatherDataRepository.findLatestByWeatherRegionId(testRegion.getId());

            // Then
            assertThat(result).isEmpty();
        }
    }

    // Helper 메서드
    private WeatherData createShortFcstData(Instant forecastAt, Double temperature, Double humidity) {
        return WeatherData.builder()
                .weatherRegion(testRegion)
                .forecastKind(ForecastKind.SHORT_FCST)
                .forecastAt(forecastAt)
                .forecastedAt(forecastAt.minus(3, ChronoUnit.HOURS))
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(temperature)
                .temperatureMin(temperature - 2.0)
                .temperatureMax(temperature + 5.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(10.0)
                .humidityCurrent(humidity)
                .humidityComparedToDayBefore(0.0)
                .windSpeed(2.5)
                .windAsWord(WindStrength.WEAK)
                .build();
    }
}
