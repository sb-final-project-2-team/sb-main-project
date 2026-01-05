package com.codeit.closet.module.weather.repository;

import com.codeit.closet.module.weather.entity.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WeatherDataRepository extends JpaRepository<WeatherData, UUID> {

    List<WeatherData> findByWeatherRegionId(UUID weatherRegionId);
}
