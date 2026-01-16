package com.codeit.closet.module.weather.repository;

import com.codeit.closet.module.weather.entity.ForecastKind;
import com.codeit.closet.module.weather.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WeatherDataRepository extends JpaRepository<WeatherData, UUID> {

    List<WeatherData> findByWeatherRegionId(UUID weatherRegionId);

    /**
     * 특정 시간대에 가장 가까운 초단기실황 데이터 조회
     * 어제 대비 데이터 계산에 사용
     */
    @Query(value = """
        SELECT * FROM weather_data w
        WHERE w.weather_region_id = :weatherRegionId
          AND w.forecast_kind = :forecastKind
          AND w.forecast_at BETWEEN :startTime AND :endTime
        ORDER BY ABS(EXTRACT(EPOCH FROM w.forecast_at) - EXTRACT(EPOCH FROM CAST(:targetTime AS TIMESTAMP WITH TIME ZONE)))
        LIMIT 1
        """, nativeQuery = true)
    Optional<WeatherData> findClosestByWeatherRegionIdAndTime(
        @Param("weatherRegionId") UUID weatherRegionId,
        @Param("forecastKind") String forecastKind,
        @Param("targetTime") Instant targetTime,
        @Param("startTime") Instant startTime,
        @Param("endTime") Instant endTime
    );

    /**
     * 특정 지역의 가장 최신 날씨 데이터 조회 (추천 기능용)
     * 가장 최근에 생성된 데이터 반환
     */
    @Query("""
        SELECT w FROM WeatherData w
        WHERE w.weatherRegion.id = :weatherRegionId
        ORDER BY w.forecastedAt DESC, w.forecastAt DESC
        LIMIT 1
        """)
    Optional<WeatherData> findLatestByWeatherRegionId(@Param("weatherRegionId") UUID weatherRegionId);
}
