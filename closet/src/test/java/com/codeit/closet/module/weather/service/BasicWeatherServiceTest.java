package com.codeit.closet.module.weather.service;

import com.codeit.closet.module.weather.client.KakaoApiClient;
import com.codeit.closet.module.weather.client.KmaApiClient;
import com.codeit.closet.module.weather.converter.KmaApiConverter;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.entity.*;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import com.codeit.closet.module.weather.service.impl.BasicWeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.codeit.closet.module.weather.exception.WeatherDataCollectionException;
import com.codeit.closet.module.weather.exception.WeatherRegionNotFoundException;

import com.codeit.closet.module.weather.dto.api.KmaApiResponse;

@ExtendWith(MockitoExtension.class)
@DisplayName("BasicWeatherService 테스트")
class BasicWeatherServiceTest {

    @Mock
    private WeatherRegionRepository weatherRegionRepository;

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private WeatherMapper weatherMapper;

    @Mock
    private KmaApiClient kmaApiClient;

    @Mock
    private KmaApiConverter kmaApiConverter;

    @Mock
    private KakaoApiClient kakaoApiClient;

    @InjectMocks
    private BasicWeatherService weatherService;

    private UUID weatherRegionId;
    private WeatherRegion weatherRegion;
    private WeatherData currentData;
    private WeatherAPILocation location;

    @BeforeEach
    void setUp() {
        weatherRegionId = UUID.randomUUID();

        // WeatherRegion 설정
        weatherRegion = WeatherRegion.builder()
                .id(weatherRegionId)
                .x(60)
                .y(127)
                .latitude(37.5665)
                .longitude(126.9780)
                .locationNames("서울특별시,종로구,청운동")
                .build();

        // 현재 시간
        Instant now = Instant.now();

        // 현재 날씨 데이터 (초단기실황)
        currentData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)
                .temperatureCompPrevDay(null) // 아직 계산 전
                .temperatureMin(10.0)
                .temperatureMax(20.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(0.0)
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(0.0) // nullable이 아니므로 기본값 설정
                .windSpeed(2.5)
                .windAsWord(WindStrength.WEAK)
                .createdAt(now)
                .updatedAt(now)
                .build();

        // WeatherAPILocation 설정
        location = new WeatherAPILocation(
                37.5665,
                126.9780,
                60,
                127,
                List.of("서울특별시", "종로구", "청운동")
        );
    }

    @Test
    @DisplayName("어제 데이터가 있을 때 어제 대비 계산이 정상적으로 수행됨")
    void findWeather_withYesterdayData_calculatesComparedToDayBefore() {
        // Given
        Double longitude = 126.9780;
        Double latitude = 37.5665;

        given(weatherRegionRepository.findByXAndY(anyInt(), anyInt()))
                .willReturn(Optional.of(weatherRegion));

        given(weatherDataRepository.findByWeatherRegionId(weatherRegionId))
                .willReturn(List.of(currentData));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(),
                            data.getForecastedAt(),
                            data.getForecastAt(),
                            location,
                            data.getSkyStatus(),
                            null, null, null, null
                    );
                });

        // When
        List<WeatherDTO> result = weatherService.findWeather(longitude, latitude);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("어제 데이터가 없을 때 어제 대비 값이 null로 유지됨")
    void findWeather_withoutYesterdayData_keepsNullValues() {
        // Given
        Double longitude = 126.9780;
        Double latitude = 37.5665;

        given(weatherRegionRepository.findByXAndY(anyInt(), anyInt()))
                .willReturn(Optional.of(weatherRegion));

        given(weatherDataRepository.findByWeatherRegionId(weatherRegionId))
                .willReturn(List.of(currentData));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(),
                            data.getForecastedAt(),
                            data.getForecastAt(),
                            location,
                            data.getSkyStatus(),
                            null, null, null, null
                    );
                });

        // When
        List<WeatherDTO> result = weatherService.findWeather(longitude, latitude);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("초단기실황(ULTRA_NOW)만 어제 대비 계산이 수행됨")
    void findWeather_onlyUltraNowCalculatesComparedToDayBefore() {
        // Given
        Double longitude = 126.9780;
        Double latitude = 37.5665;

        Instant now = Instant.now();

        // 단기예보 데이터 추가
        WeatherData shortFcstData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.SHORT_FCST) // 단기예보
                .forecastAt(now.plus(3, ChronoUnit.HOURS))
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(18.0)
                .temperatureCompPrevDay(null)
                .temperatureMin(12.0)
                .temperatureMax(22.0)
                .precipitationType(PrecipitationType.NONE)
                .precipitationAmount(0.0)
                .precipitationProb(10.0)
                .humidityCurrent(55.0)
                .humidityComparedToDayBefore(0.0)
                .windSpeed(3.0)
                .windAsWord(WindStrength.WEAK)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(weatherRegionRepository.findByXAndY(anyInt(), anyInt()))
                .willReturn(Optional.of(weatherRegion));

        given(weatherDataRepository.findByWeatherRegionId(weatherRegionId))
                .willReturn(List.of(currentData, shortFcstData));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(),
                            data.getForecastedAt(),
                            data.getForecastAt(),
                            location,
                            data.getSkyStatus(),
                            null, null, null, null
                    );
                });

        // When
        List<WeatherDTO> result = weatherService.findWeather(longitude, latitude);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("어제 대비 계산 시 SHORT_FCST 데이터로 비교")
    void collectUltraSrtNcst_comparesWithShortFcstData() {
        // Given
        Integer nx = 60;
        Integer ny = 127;
        Instant now = Instant.now();

        // 어제 단기예보 데이터 (SHORT_FCST)
        WeatherData yesterdayShortFcst = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.SHORT_FCST)
                .forecastAt(now.minus(24, ChronoUnit.HOURS))
                .forecastedAt(now.minus(24, ChronoUnit.HOURS))
                .skyStatus(SkyStatus.CLOUDY)
                .temperatureCurrent(10.0)  // 어제 온도
                .humidityCurrent(50.0)     // 어제 습도
                .humidityComparedToDayBefore(0.0)
                .build();

        // 초단기실황 API 응답 Mock
        KmaApiResponse mockResponse = new KmaApiResponse();

        // 변환된 현재 날씨 데이터
        WeatherData convertedData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)  // 현재 온도 (어제보다 +5도)
                .humidityCurrent(60.0)     // 현재 습도 (어제보다 +10%)
                .humidityComparedToDayBefore(0.0)
                .build();

        given(weatherRegionRepository.findByXAndY(nx, ny))
                .willReturn(Optional.of(weatherRegion));

        given(kmaApiClient.getUltraSrtNcst(nx, ny))
                .willReturn(mockResponse);

        given(kmaApiConverter.convertUltraSrtNcst(mockResponse, weatherRegion))
                .willReturn(convertedData);

        // SHORT_FCST 데이터로 어제 비교 데이터 조회
        given(weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                eq(weatherRegionId),
                eq("SHORT_FCST"),  // SHORT_FCST로 조회하는지 검증
                any(Instant.class),
                any(Instant.class),
                any(Instant.class)
        )).willReturn(Optional.of(yesterdayShortFcst));

        given(weatherDataRepository.save(any(WeatherData.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(),
                            data.getForecastedAt(),
                            data.getForecastAt(),
                            location,
                            data.getSkyStatus(),
                            null, null, null, null
                    );
                });

        // When
        weatherService.collectUltraSrtNcst(nx, ny);

        // Then
        // SHORT_FCST 데이터로 비교 조회가 호출되었는지 검증
        verify(weatherDataRepository).findClosestByWeatherRegionIdAndTime(
                eq(weatherRegionId),
                eq("SHORT_FCST"),  // ULTRA_NOW가 아닌 SHORT_FCST로 조회
                any(Instant.class),
                any(Instant.class),
                any(Instant.class)
        );

        // 온도/습도 차이 계산 검증 (현재 - 어제)
        assertThat(convertedData.getTemperatureCompPrevDay()).isEqualTo(5.0);  // 15.0 - 10.0
        assertThat(convertedData.getHumidityComparedToDayBefore()).isEqualTo(10.0);  // 60.0 - 50.0
    }

    @Test
    @DisplayName("어제 SHORT_FCST 데이터가 없으면 비교값이 null로 유지됨")
    void collectUltraSrtNcst_withoutYesterdayData_keepsNullValues() {
        // Given
        Integer nx = 60;
        Integer ny = 127;
        Instant now = Instant.now();

        KmaApiResponse mockResponse = new KmaApiResponse();

        WeatherData convertedData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)
                .temperatureCompPrevDay(null)  // 초기값 null
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(0.0)
                .build();

        given(weatherRegionRepository.findByXAndY(nx, ny))
                .willReturn(Optional.of(weatherRegion));

        given(kmaApiClient.getUltraSrtNcst(nx, ny))
                .willReturn(mockResponse);

        given(kmaApiConverter.convertUltraSrtNcst(mockResponse, weatherRegion))
                .willReturn(convertedData);

        // 어제 데이터 없음
        given(weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                eq(weatherRegionId),
                eq("SHORT_FCST"),
                any(Instant.class),
                any(Instant.class),
                any(Instant.class)
        )).willReturn(Optional.empty());

        given(weatherDataRepository.save(any(WeatherData.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(),
                            data.getForecastedAt(),
                            data.getForecastAt(),
                            location,
                            data.getSkyStatus(),
                            null, null, null, null
                    );
                });

        // When
        weatherService.collectUltraSrtNcst(nx, ny);

        // Then
        // 비교값이 null로 유지되는지 검증
        assertThat(convertedData.getTemperatureCompPrevDay()).isNull();
    }

    // ===== 예외 처리 테스트 케이스 =====

    @Test
    @DisplayName("미등록 격자 좌표로 초단기실황 수집 시 예외 발생")
    void collectUltraSrtNcst_regionNotFound_throwsException() {
        // Given
        Integer nx = 999;
        Integer ny = 999;

        given(weatherRegionRepository.findByXAndY(nx, ny))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> weatherService.collectUltraSrtNcst(nx, ny))
                .isInstanceOf(WeatherRegionNotFoundException.class)
                .hasMessageContaining("해당 격자 좌표의 지역이 등록되지 않았습니다");
    }

    @Test
    @DisplayName("미등록 격자 좌표로 단기예보 수집 시 예외 발생")
    void collectVilageFcst_regionNotFound_throwsException() {
        // Given
        Integer nx = 999;
        Integer ny = 999;

        given(weatherRegionRepository.findByXAndY(nx, ny))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> weatherService.collectVilageFcst(nx, ny))
                .isInstanceOf(WeatherRegionNotFoundException.class)
                .hasMessageContaining("해당 격자 좌표의 지역이 등록되지 않았습니다");
    }

    @Test
    @DisplayName("유효한 ID로 지역별 초단기실황 수집 성공")
    void collectUltraSrtNcstForRegion_success() {
        // Given
        Instant now = Instant.now();
        KmaApiResponse mockResponse = new KmaApiResponse();

        WeatherData convertedData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.ULTRA_NOW)
                .forecastAt(now)
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(15.0)
                .humidityCurrent(60.0)
                .humidityComparedToDayBefore(0.0)
                .build();

        given(weatherRegionRepository.findById(weatherRegionId))
                .willReturn(Optional.of(weatherRegion));

        given(weatherRegionRepository.findByXAndY(weatherRegion.getX(), weatherRegion.getY()))
                .willReturn(Optional.of(weatherRegion));

        given(kmaApiClient.getUltraSrtNcst(weatherRegion.getX(), weatherRegion.getY()))
                .willReturn(mockResponse);

        given(kmaApiConverter.convertUltraSrtNcst(mockResponse, weatherRegion))
                .willReturn(convertedData);

        given(weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                eq(weatherRegionId), eq("SHORT_FCST"), any(), any(), any()))
                .willReturn(Optional.empty());

        given(weatherDataRepository.save(any(WeatherData.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(), data.getForecastedAt(), data.getForecastAt(),
                            location, data.getSkyStatus(), null, null, null, null
                    );
                });

        // When
        WeatherDTO result = weatherService.collectUltraSrtNcstForRegion(weatherRegionId);

        // Then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("미등록 ID로 지역별 초단기실황 수집 시 예외 발생")
    void collectUltraSrtNcstForRegion_notFound_throwsException() {
        // Given
        UUID invalidId = UUID.randomUUID();

        given(weatherRegionRepository.findById(invalidId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> weatherService.collectUltraSrtNcstForRegion(invalidId))
                .isInstanceOf(WeatherRegionNotFoundException.class)
                .hasMessageContaining("WeatherRegion을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("유효한 ID로 지역별 단기예보 수집 성공")
    void collectVilageFcstForRegion_success() {
        // Given
        Instant now = Instant.now();
        KmaApiResponse mockResponse = new KmaApiResponse();

        WeatherData forecastData = WeatherData.builder()
                .id(UUID.randomUUID())
                .weatherRegion(weatherRegion)
                .forecastKind(ForecastKind.SHORT_FCST)
                .forecastAt(now.plus(3, ChronoUnit.HOURS))
                .forecastedAt(now)
                .skyStatus(SkyStatus.CLEAR)
                .temperatureCurrent(18.0)
                .precipitationProb(10.0)
                .humidityCurrent(55.0)
                .humidityComparedToDayBefore(0.0)
                .build();

        given(weatherRegionRepository.findById(weatherRegionId))
                .willReturn(Optional.of(weatherRegion));

        given(weatherRegionRepository.findByXAndY(weatherRegion.getX(), weatherRegion.getY()))
                .willReturn(Optional.of(weatherRegion));

        given(kmaApiClient.getVilageFcst(weatherRegion.getX(), weatherRegion.getY()))
                .willReturn(mockResponse);

        given(kmaApiConverter.convertVilageFcst(mockResponse, weatherRegion))
                .willReturn(List.of(forecastData));

        given(weatherDataRepository.saveAll(anyList()))
                .willAnswer(invocation -> invocation.getArgument(0));

        given(weatherMapper.toWeatherDTO(any(WeatherData.class)))
                .willAnswer(invocation -> {
                    WeatherData data = invocation.getArgument(0);
                    return new WeatherDTO(
                            data.getId(), data.getForecastedAt(), data.getForecastAt(),
                            location, data.getSkyStatus(), null, null, null, null
                    );
                });

        // When
        List<WeatherDTO> result = weatherService.collectVilageFcstForRegion(weatherRegionId);

        // Then
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("미등록 ID로 지역별 단기예보 수집 시 예외 발생")
    void collectVilageFcstForRegion_notFound_throwsException() {
        // Given
        UUID invalidId = UUID.randomUUID();

        given(weatherRegionRepository.findById(invalidId))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> weatherService.collectVilageFcstForRegion(invalidId))
                .isInstanceOf(WeatherRegionNotFoundException.class)
                .hasMessageContaining("WeatherRegion을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("날씨 데이터 수집 실패 시 WeatherDataCollectionException 발생")
    void findWeather_apiCollectionFails_throwsException() {
        // Given
        Double longitude = 126.9780;
        Double latitude = 37.5665;

        given(weatherRegionRepository.findByXAndY(anyInt(), anyInt()))
                .willReturn(Optional.of(weatherRegion));

        // 데이터가 없어서 API 호출 시도
        given(weatherDataRepository.findByWeatherRegionId(weatherRegionId))
                .willReturn(List.of());

        // API 호출 시 예외 발생
        given(kmaApiClient.getUltraSrtNcst(anyInt(), anyInt()))
                .willThrow(new RuntimeException("API 연결 실패"));

        // When & Then
        assertThatThrownBy(() -> weatherService.findWeather(longitude, latitude))
                .isInstanceOf(WeatherDataCollectionException.class)
                .hasMessageContaining("날씨 데이터 수집에 실패했습니다");
    }
}
