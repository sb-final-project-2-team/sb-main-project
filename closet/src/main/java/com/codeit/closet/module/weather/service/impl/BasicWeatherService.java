package com.codeit.closet.module.weather.service.impl;

import com.codeit.closet.module.weather.client.KmaApiClient;
import com.codeit.closet.module.weather.converter.KmaApiConverter;
import com.codeit.closet.module.weather.dto.api.KmaApiResponse;
import com.codeit.closet.module.weather.dto.location.LocationDTO;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.entity.ForecastKind;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import com.codeit.closet.module.weather.mapper.WeatherMapper;
import com.codeit.closet.module.weather.repository.WeatherDataRepository;
import com.codeit.closet.module.weather.repository.WeatherRegionRepository;
import com.codeit.closet.module.weather.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BasicWeatherService implements WeatherService {

    private final WeatherRegionRepository weatherRegionRepository;
    private final WeatherDataRepository weatherDataRepository;
    private final WeatherMapper weatherMapper;
    private final KmaApiClient kmaApiClient;
    private final KmaApiConverter kmaApiConverter;

    @Override
    @Transactional
    public List<WeatherDTO> findWeather(Double longitude, Double latitude) {
        GridCoordinates grid = convertToGrid(longitude, latitude);
        log.info("좌표 변환: (lon={}, lat={}) → 격자({}, {})", longitude, latitude, grid.x(), grid.y());

        WeatherRegion region = weatherRegionRepository.findByXAndY(grid.x(), grid.y())
                .orElseGet(() -> createWeatherRegion(grid.x(), grid.y(), longitude, latitude));

        List<WeatherData> dataList = weatherDataRepository.findByWeatherRegionId(region.getId());
        if (dataList.isEmpty()) {
            log.info("날씨 데이터 없음. API 호출하여 수집: regionId={}", region.getId());
            try {
                collectUltraSrtNcst(grid.x(), grid.y());
                collectVilageFcst(grid.x(), grid.y());
                dataList = weatherDataRepository.findByWeatherRegionId(region.getId());
            } catch (Exception e) {
                log.error("날씨 데이터 수집 실패: {}", e.getMessage(), e);
                throw new IllegalStateException("날씨 데이터 수집에 실패했습니다: " + e.getMessage());
            }
        }

        // 오늘(초단기실황) + 내일~5일후(단기예보) 일별 대표 데이터로 집계 (최대 6개)
        List<WeatherData> aggregatedList = filterAndAggregateDailyWeather(dataList);

        return aggregatedList.stream()
                .map(weatherMapper::toWeatherDTO)
                .toList();
    }

    /**
     * 오늘~5일 후 날씨를 일별 대표 데이터로 집계
     */
    private List<WeatherData> filterAndAggregateDailyWeather(List<WeatherData> dataList) {
        ZoneId koreaZone = ZoneId.of("Asia/Seoul");
        LocalDate today = LocalDate.now(koreaZone);
        LocalDate maxDate = today.plusDays(5);

        List<WeatherData> result = new ArrayList<>();

        // 오늘 날짜의 단기예보에서 최저/최고 온도 추출
        List<WeatherData> todayForecasts = dataList.stream()
                .filter(data -> data.getForecastKind() == ForecastKind.SHORT_FCST)
                .filter(data -> data.getForecastAt().atZone(koreaZone).toLocalDate().equals(today))
                .toList();

        Double todayMin = todayForecasts.stream()
                .map(WeatherData::getTemperatureMin)
                .filter(temp -> temp != null && temp < 100) // 유효한 값만
                .min(Double::compareTo)
                .orElse(null);

        Double todayMax = todayForecasts.stream()
                .map(WeatherData::getTemperatureMax)
                .filter(temp -> temp != null && temp > -100) // 유효한 값만
                .max(Double::compareTo)
                .orElse(null);

        // 오늘: 초단기실황 + 단기예보의 최저/최고 온도 병합
        dataList.stream()
                .filter(data -> data.getForecastKind() == ForecastKind.ULTRA_NOW)
                .findFirst()
                .ifPresent(ultraNow -> {
                    if (todayMin != null) {
                        ultraNow.setTemperatureMin(todayMin);
                    }
                    if (todayMax != null) {
                        ultraNow.setTemperatureMax(todayMax);
                    }
                    result.add(ultraNow);
                });

        // 내일~5일후: 단기예보를 일별로 그룹화
        Map<LocalDate, List<WeatherData>> dailyForecastMap = dataList.stream()
                .filter(data -> data.getForecastKind() == ForecastKind.SHORT_FCST)
                .filter(data -> {
                    LocalDate forecastDate = data.getForecastAt().atZone(koreaZone).toLocalDate();
                    return !forecastDate.isBefore(today) && !forecastDate.isAfter(maxDate);
                })
                .collect(Collectors.groupingBy(
                        data -> data.getForecastAt().atZone(koreaZone).toLocalDate()
                ));

        // 일별 대표 데이터 선택
        dailyForecastMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> !entry.getKey().equals(today))
                .forEach(entry -> {
                    WeatherData representative = selectDailyRepresentative(entry.getValue(), koreaZone);
                    if (representative != null) {
                        result.add(representative);
                    }
                });

        result.sort(Comparator.comparing(WeatherData::getForecastAt));
        log.info("날씨 집계: {}건 → {}건", dataList.size(), result.size());

        return result;
    }

    /**
     * 일별 대표 데이터 선택 (우선순위: 12시 → 15시 → 9시 → 18시 → 6시)
     */
    private WeatherData selectDailyRepresentative(List<WeatherData> dayData, ZoneId zone) {
        if (dayData.isEmpty()) {
            return null;
        }

        int[] preferredHours = {12, 15, 9, 18, 6};
        for (int hour : preferredHours) {
            for (WeatherData data : dayData) {
                if (data.getForecastAt().atZone(zone).getHour() == hour) {
                    return data;
                }
            }
        }

        return dayData.stream()
                .min(Comparator.comparingInt(data ->
                        Math.abs(data.getForecastAt().atZone(zone).getHour() - 12)))
                .orElse(dayData.get(0));
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherAPILocation findWeatherLocation(Double longitude, Double latitude) {
        GridCoordinates grid = convertToGrid(longitude, latitude);

        WeatherRegion region = weatherRegionRepository.findByXAndY(grid.x(), grid.y())
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 좌표의 날씨 지역을 찾을 수 없습니다: lon=" + longitude + ", lat=" + latitude
                ));

        return weatherMapper.toWeatherAPILocation(region);
    }

    @Override
    @Transactional
    public WeatherDTO collectUltraSrtNcst(Integer nx, Integer ny) {
        log.info("초단기실황 수집: nx={}, ny={}", nx, ny);

        WeatherRegion region = weatherRegionRepository.findByXAndY(nx, ny)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 격자 좌표의 지역이 등록되지 않았습니다: nx=" + nx + ", ny=" + ny));

        KmaApiResponse response = kmaApiClient.getUltraSrtNcst(nx, ny);
        WeatherData weatherData = kmaApiConverter.convertUltraSrtNcst(response, region);
        WeatherData saved = weatherDataRepository.save(weatherData);
        updateLastCollectedAt(region);

        log.info("초단기실황 수집 완료: id={}", saved.getId());
        return weatherMapper.toWeatherDTO(saved);
    }

    @Override
    @Transactional
    public List<WeatherDTO> collectVilageFcst(Integer nx, Integer ny) {
        log.info("단기예보 수집: nx={}, ny={}", nx, ny);

        WeatherRegion region = weatherRegionRepository.findByXAndY(nx, ny)
                .orElseThrow(() -> new IllegalArgumentException(
                        "해당 격자 좌표의 지역이 등록되지 않았습니다: nx=" + nx + ", ny=" + ny));

        KmaApiResponse response = kmaApiClient.getVilageFcst(nx, ny);
        List<WeatherData> weatherDataList = kmaApiConverter.convertVilageFcst(response, region);
        List<WeatherData> savedList = weatherDataRepository.saveAll(weatherDataList);
        updateLastCollectedAt(region);

        log.info("단기예보 수집 완료: {}건", savedList.size());

        return savedList.stream()
                .map(weatherMapper::toWeatherDTO)
                .toList();
    }

    @Override
    @Transactional
    public WeatherDTO collectUltraSrtNcstForRegion(UUID weatherRegionId) {
        WeatherRegion region = weatherRegionRepository.findById(weatherRegionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WeatherRegion을 찾을 수 없습니다: " + weatherRegionId
                ));

        return collectUltraSrtNcst(region.getX(), region.getY());
    }

    @Override
    @Transactional
    public List<WeatherDTO> collectVilageFcstForRegion(UUID weatherRegionId) {
        WeatherRegion region = weatherRegionRepository.findById(weatherRegionId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "WeatherRegion을 찾을 수 없습니다: " + weatherRegionId
                ));

        return collectVilageFcst(region.getX(), region.getY());
    }

    @Transactional
    protected WeatherRegion createWeatherRegion(Integer x, Integer y, Double longitude, Double latitude) {
        WeatherRegion newRegion = WeatherRegion.builder()
                .x(x)
                .y(y)
                .latitude(latitude)
                .longitude(longitude)
                .locationNames(String.format("격자(%d, %d)", x, y))
                .build();

        WeatherRegion saved = weatherRegionRepository.save(newRegion);
        log.info("WeatherRegion 생성: id={}, 격자({}, {})", saved.getId(), x, y);
        return saved;
    }

    @Transactional
    protected void updateLastCollectedAt(WeatherRegion region) {
        weatherRegionRepository.findById(region.getId())
                .ifPresent(WeatherRegion::updateLastCollectedAt);
    }

    /**
     * WGS84 → 기상청 격자 좌표 변환 (Lambert Conformal Conic 투영법)
     */
    private GridCoordinates convertToGrid(Double longitude, Double latitude) {
        final double RE = 6371.00877, GRID = 5.0;
        final double SLAT1 = 30.0, SLAT2 = 60.0, OLON = 126.0, OLAT = 38.0;
        final double XO = 43, YO = 136;
        final double DEGRAD = Math.PI / 180.0;

        double re = RE / GRID;
        double slat1 = SLAT1 * DEGRAD, slat2 = SLAT2 * DEGRAD;
        double olon = OLON * DEGRAD, olat = OLAT * DEGRAD;

        double sn = Math.log(Math.cos(slat1) / Math.cos(slat2)) /
                Math.log(Math.tan(Math.PI * 0.25 + slat2 * 0.5) / Math.tan(Math.PI * 0.25 + slat1 * 0.5));
        double sf = Math.pow(Math.tan(Math.PI * 0.25 + slat1 * 0.5), sn) * Math.cos(slat1) / sn;
        double ro = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + olat * 0.5), sn);
        double ra = re * sf / Math.pow(Math.tan(Math.PI * 0.25 + latitude * DEGRAD * 0.5), sn);

        double theta = longitude * DEGRAD - olon;
        if (theta > Math.PI) theta -= 2.0 * Math.PI;
        if (theta < -Math.PI) theta += 2.0 * Math.PI;
        theta *= sn;

        int x = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
        int y = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
        return new GridCoordinates(x, y);
    }

    private record GridCoordinates(Integer x, Integer y) {}

    /**
     * 어제 대비 온도/습도 차이 계산 (초단기실황 전용)
     */
    private void calculateComparedToDayBefore(WeatherData current, UUID weatherRegionId) {
        try {
            Instant yesterday = current.getForecastAt().minus(24, ChronoUnit.HOURS);
            Instant startTime = yesterday.minus(1, ChronoUnit.HOURS);
            Instant endTime = yesterday.plus(1, ChronoUnit.HOURS);

            weatherDataRepository.findClosestByWeatherRegionIdAndTime(
                    weatherRegionId, "ULTRA_NOW", yesterday, startTime, endTime
            ).ifPresent(yesterdayData -> {
                current.setTemperatureCompPrevDay(
                        current.getTemperatureCurrent() - yesterdayData.getTemperatureCurrent());
                current.setHumidityComparedToDayBefore(
                        current.getHumidityCurrent() - yesterdayData.getHumidityCurrent());
            });
        } catch (Exception e) {
            log.warn("어제 대비 계산 실패: {}", e.getMessage());
        }
    }
}