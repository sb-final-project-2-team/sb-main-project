package com.codeit.closet.module.weather.service.impl;

import com.codeit.closet.module.weather.dto.location.LocationDTO;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import com.codeit.closet.module.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BasicWeatherService implements WeatherService {

    private final WeatherRegionRepository weatherRegionRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherMapper weatherMapper;

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDTO> findWeather(Double longitude, Double latitude) {
        GridCoordinates grid = convertToGrid(longitude, latitude);

        WeatherRegion region = weatherRegionRepository.findByXAndY(grid.x(), grid.y())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 좌표의 날씨 지역을 찾을 수 없습니다: lon=" + longitude + ", lat=" + latitude
                ));

        List<WeatherData> dataList = weatherDataRepository.findByWeatherRegionId(region.getId());
        if (dataList.isEmpty()) {
            throw new IllegalArgumentException(
                    "해당 지역의 날씨 데이터가 없습니다: x=" + grid.x() + ", y=" + grid.y()
            );
        }

        LocationDTO location = weatherMapper.toLocationDTO(region, latitude, longitude);

        return dataList.stream()
                .map(data -> weatherMapper.toWeatherDTO(data, location))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherAPILocation findWeatherLocation(Double longitude, Double latitude) {
        GridCoordinates grid = convertToGrid(longitude, latitude);

        WeatherRegion region = weatherRegionRepository.findByXAndY(grid.x(), grid.y())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 좌표의 날씨 지역을 찾을 수 없습니다: lon=" + longitude + ", lat=" + latitude
                ));

        return weatherMapper.toWeatherAPILocation(region, latitude, longitude);
    }

    /**
     * WGS84 좌표 → 기상청 격자 좌표 변환
     * 실제 변환 알고리즘 구현 필요
     * 참고자료: (https://gist.github.com/fronteer-kr/14d7f779d52a21ac2f16)
     */
    private GridCoordinates convertToGrid(Double longitude, Double latitude) {
        return new GridCoordinates(61, 126);  // 임시: 서울 강남 고정값
    }

    private record GridCoordinates(Integer x, Integer y) {
    }
}
