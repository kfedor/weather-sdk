package com.github.kfedor.weather.sdk.http;

/**
 * Holds base endpoint URLs and constants used for HTTP communication
 * with the OpenWeather API.
 *
 * <p>This class centralizes configuration of remote endpoints such as
 * the weather and geocoding APIs. Keeping URLs in one place makes it
 * easier to maintain and update provider settings if they change.</p>
 *
 * <p>All values are immutable and intended for internal use by
 * HTTP clients like {@link com.github.kfedor.weather.sdk.core.WeatherApiClient}
 * and {@link com.github.kfedor.weather.sdk.core.GeocodingClient}.</p>
 */
public class HttpConfig {
    public static final String WEATHER_ENDPOINT = "https://api.openweathermap.org/data/2.5/weather";
    public static final String GEOCODING_ENDPOINT = "http://api.openweathermap.org/geo/1.0/direct";
}
