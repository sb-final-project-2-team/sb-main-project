package com.codeit.closet.module.weather.controller;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weathers")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @GetMapping
    public ResponseEntity<List<WeatherDTO>> getWeathers(
            @RequestParam Double longitude,
            @RequestParam Double latitude
    ) {
        List<WeatherDTO> result = weatherService.findWeather(longitude, latitude);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/location")
    public ResponseEntity<WeatherAPILocation> getWeatherLocation(
            @RequestParam Double longitude,
            @RequestParam Double latitude
    ) {
        WeatherAPILocation result = weatherService.findWeatherLocation(longitude, latitude);
        return ResponseEntity.status(HttpStatus.OK).body(result);
    }
}
