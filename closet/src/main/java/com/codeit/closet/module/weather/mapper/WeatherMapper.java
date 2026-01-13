package com.codeit.closet.module.weather.mapper;

import com.codeit.closet.module.weather.dto.location.WeatherAPILocation;
import com.codeit.closet.module.weather.dto.weather.HumidityDTO;
import com.codeit.closet.module.weather.dto.weather.PrecipitationDTO;
import com.codeit.closet.module.weather.dto.weather.TemperatureDTO;
import com.codeit.closet.module.weather.dto.weather.WeatherDTO;
import com.codeit.closet.module.weather.dto.weather.WeatherSummaryDTO;
import com.codeit.closet.module.weather.dto.weather.WindSpeedDTO;
import com.codeit.closet.module.weather.entity.WeatherData;
import com.codeit.closet.module.weather.entity.WeatherRegion;
import java.util.Arrays;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

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

  @Mapping(target = "weatherId", source = "id")
  @Mapping(target = "skyStatus", source = "weatherData.skyStatus")
  @Mapping(target = "precipitation", source = "weatherData")
  @Mapping(target = "temperature", source = "weatherData")
  WeatherSummaryDTO toWeatherSummaryDTO(WeatherRegion weather);

  @Named("stringToList")
  default List<String> stringToList(String locationNames) {
    if (locationNames == null || locationNames.isBlank()) {
      return List.of();
    }
    return Arrays.stream(locationNames.split(","))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .toList();
  }
}
