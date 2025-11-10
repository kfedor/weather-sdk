package com.github.kfedor.weather.sdk.model;

import lombok.Getter;
import lombok.Setter;

/**
 * Represents the standardized weather data structure returned by the SDK.
 *
 * <p>This is the public response model provided to SDK users — a simplified,
 * consistent abstraction of the raw OpenWeather API response. It contains
 * only the most relevant weather attributes such as temperature, conditions,
 * wind speed, visibility, and time-related data.</p>
 *
 * <p>Unlike {@link com.github.kfedor.weather.sdk.model.OpenWeatherResponse},
 * which mirrors the provider’s raw JSON format, this class defines a stable
 * contract that does not depend on provider-specific fields.</p>
 *
 * <p>Instances of this class are typically returned by
 * {@link com.github.kfedor.weather.sdk.WeatherSdk#getCurrentByCity(String)}
 * and related methods.</p>
 *
 * @see com.github.kfedor.weather.sdk.model.OpenWeatherResponse
 * @see com.github.kfedor.weather.sdk.mapper.WeatherMapper
 */
@Getter
@Setter
public class WeatherResponse {
    private Weather weather;
    private Temperature temperature;
    private int visibility;
    private Wind wind;
    private long datetime;
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
    public static class Temperature {
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
