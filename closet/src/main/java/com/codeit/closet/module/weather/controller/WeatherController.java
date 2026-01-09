package com.codeit.closet.module.weather.controller;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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

    @PostMapping("/collect/ultra-srt-ncst")
    public ResponseEntity<WeatherDTO> collectUltraSrtNcst(
            @RequestParam Integer nx,
            @RequestParam Integer ny
    ) {
        WeatherDTO result = weatherService.collectUltraSrtNcst(nx, ny);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/collect/vilag-fcst")
    public ResponseEntity<List<WeatherDTO>> collectVilageFcst(
            @RequestParam Integer nx,
            @RequestParam Integer ny
    ) {
        List<WeatherDTO> result = weatherService.collectVilageFcst(nx, ny);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/collect/ultra-srt-ncst/region/{weatherRegionId}")
    public ResponseEntity<WeatherDTO> collectUltraSrtNcstForRegion(
            @PathVariable UUID weatherRegionId
    ) {
        WeatherDTO result = weatherService.collectUltraSrtNcstForRegion(weatherRegionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PostMapping("/collect/vilag-fcst/region/{weatherRegionId}")
    public ResponseEntity<List<WeatherDTO>> collectVilageFcstForRegion(
            @PathVariable UUID weatherRegionId
    ) {
        List<WeatherDTO> result = weatherService.collectVilageFcstForRegion(weatherRegionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
