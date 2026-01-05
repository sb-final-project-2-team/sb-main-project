package com.codeit.closet.module.weather.repository;

import com.codeit.closet.module.weather.entity.WeatherRegion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WeatherRegionRepository extends JpaRepository<WeatherRegion, UUID> {

    Optional<WeatherRegion> findByXAndY(Integer x, Integer y);
}
