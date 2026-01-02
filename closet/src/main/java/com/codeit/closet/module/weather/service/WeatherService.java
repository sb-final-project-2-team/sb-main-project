package com.codeit.closet.module.weather.service;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;

import java.util.List;

public interface WeatherService {

    List<WeatherDTO> findWeather(Double longitude, Double latitude);

    WeatherAPILocation findWeatherLocation(Double longitude, Double latitude);
}
