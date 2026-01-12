package com.codeit.closet.module.weather.service;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;

import com.codeit.closet.module.weather.entity.WeatherRegion;
import java.util.List;
import java.util.UUID;

public interface WeatherService {

    List<WeatherDTO> findWeather(Double longitude, Double latitude);

    WeatherAPILocation findWeatherLocation(Double longitude, Double latitude);

    /**
     * 초단기실황 데이터 수집 및 저장
     */
    WeatherDTO collectUltraSrtNcst(Integer nx, Integer ny);

    /**
     * 단기예보 데이터 수집 및 저장
     */
    List<WeatherDTO> collectVilageFcst(Integer nx, Integer ny);

    /**
     * 특정 지역의 초단기실황 수집
     */
    WeatherDTO collectUltraSrtNcstForRegion(UUID weatherRegionId);

    /**
     * 특정 지역의 단기예보 수집
     */
    List<WeatherDTO> collectVilageFcstForRegion(UUID weatherRegionId);

    /**
     * 내부 API용 WeatherRegion 처리
     */
    WeatherRegion findWeatherRegion(Double longitude, Double latitude);
}
