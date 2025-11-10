package com.github.kfedor.weather.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the raw JSON response structure returned by the OpenWeather API.
 *
 * <p>This class mirrors only the fields required by the SDK — such as
 * temperature, weather conditions, wind, visibility, and timestamps —
 * and is used as an intermediate data model before conversion to
 * {@link com.github.kfedor.weather.sdk.model.WeatherResponse}.</p>
 *
 * <p>It is designed as a simple POJO compatible with JSON libraries like
 * Gson or Jackson, and supports direct deserialization from the API response.</p>
 *
 * @see com.github.kfedor.weather.sdk.mapper.WeatherMapper
 * @see com.github.kfedor.weather.sdk.core.WeatherApiClient
 */
@Getter
@Setter
public class OpenWeatherResponse {

    private Weather[] weather;
    private Main main;
    private int visibility;
    private Wind wind;
    private long dt;
    private Sys sys;
    private int timezone;
    private String name;

    @Getter
    @Setter
    public static class Weather {
        private String main;
        private String description;
    }

    @Getter
    @Setter
    public static class Main {
        private double temp;
        private double feels_like;
    }

    @Getter
    @Setter
    public static class Wind {
        private double speed;
    }

    @Getter
    @Setter
    public static class Sys {
        private long sunrise;
        private long sunset;
    }
}
