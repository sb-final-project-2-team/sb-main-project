package com.codeit.closet.module.weather.mapper;

import com.codeit.closet.module.weather.dto.location.LocationDTO;
import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.*;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Arrays;
import java.util.List;

@Mapper(componentModel = "spring")
public interface WeatherMapper {

    @Mapping(source = "temperatureCurrent", target = "current")
    @Mapping(source = "temperatureCompPrevDay", target = "comparedToDayBefore")
    @Mapping(source = "temperatureMin", target = "min")
    @Mapping(source = "temperatureMax", target = "max")
    TemperatureDTO toTemperatureDTO(WeatherData data);

    @Mapping(source = "precipitationType", target = "type")
    @Mapping(source = "precipitationAmount", target = "amount")
    @Mapping(source = "precipitationProb", target = "probability")
    PrecipitationDTO toPrecipitationDTO(WeatherData data);

    @Mapping(source = "humidityCurrent", target = "current")
    @Mapping(source = "humidityComparedToDayBefore", target = "comparedToDayBefore")
    HumidityDTO toHumidityDTO(WeatherData data);

    @Mapping(source = "windSpeed", target = "speed")
    @Mapping(source = "windAsWord", target = "asWord")
    WindSpeedDTO toWindSpeedDTO(WeatherData data);

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "x", target = "x")
    @Mapping(source = "y", target = "y")
    @Mapping(source = "locationNames", target = "locationNames", qualifiedByName = "stringToList")
    LocationDTO toLocationDTO(WeatherRegion region);

    @Mapping(target = "weatherId", source = "id")
    @Mapping(target = "skyStatus", source = "weather.weatherData.skyStatus")
    @Mapping(target = "precipitation", source = "weather.weatherData")
    @Mapping(target = "temperature", source = "weather.weatherData")
    WeatherSummaryDTO toSummary(WeatherRegion weather);

    @Mapping(source = "latitude", target = "latitude")
    @Mapping(source = "longitude", target = "longitude")
    @Mapping(source = "x", target = "x")
    @Mapping(source = "y", target = "y")
    @Mapping(source = "locationNames", target = "locationNames", qualifiedByName = "stringToList")
    WeatherAPILocation toWeatherAPILocation(WeatherRegion region);

    @Mapping(source = "data.id", target = "id")
    @Mapping(source = "data.forecastedAt", target = "forecastedAt")
    @Mapping(source = "data.forecastAt", target = "forecastAt")
    @Mapping(source = "data.weatherRegion", target = "location")
    @Mapping(source = "data.skyStatus", target = "skyStatus")
    @Mapping(source = "data", target = "precipitation")
    @Mapping(source = "data", target = "humidity")
    @Mapping(source = "data", target = "temperature")
    @Mapping(source = "data", target = "windSpeed")
    WeatherDTO toWeatherDTO(WeatherData data);

    @Named("stringToList")
    default List<String> stringToList(String locationNames) {
        if (locationNames == null || locationNames.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.asList(locationNames.split(","));
    }
}
