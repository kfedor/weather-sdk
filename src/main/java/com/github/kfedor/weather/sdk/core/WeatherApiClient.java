package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.HttpConfig;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.http.UrlBuilder;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

/**
 * Low-level HTTP client responsible for communicating with the OpenWeather API.
 *
 * <p>This class handles the actual network requests to retrieve current weather data
 * by geographic coordinates. It builds a properly formatted request URL,
 * sends it via {@link com.github.kfedor.weather.sdk.http.RequestExecutor},
 * and deserializes the JSON response into an {@link com.github.kfedor.weather.sdk.model.OpenWeatherResponse} object.</p>
 *
 * <p>Intended for internal SDK use only.</p>
 */
public class WeatherApiClient {
    private final String apiKey;
    private final RequestExecutor http;
    private final Gson gson = new Gson();

    public WeatherApiClient(String apiKey, RequestExecutor http) {
        this.apiKey = apiKey;
        this.http = http;
    }

    /**
     * Retrieves raw weather data for the specified coordinates.
     *
     * <p>Constructs the request URL using latitude, longitude, and API key,
     * performs an HTTP GET request, and parses the resulting JSON response
     * into an {@link com.github.kfedor.weather.sdk.model.OpenWeatherResponse}.</p>
     *
     * @param latitude  latitude of the location
     * @param longitude longitude of the location
     * @return a raw provider response with weather information
     * @throws com.github.kfedor.weather.sdk.exception.WeatherSdkException if a network error occurs or the provider returns invalid JSON
     */
    public OpenWeatherResponse byCoordinates(double latitude, double longitude) {
        try {
            URI uri = UrlBuilder.build(HttpConfig.WEATHER_ENDPOINT, Map.of(
                    "appid", apiKey,
                    "lat", String.format(Locale.ROOT, "%.6f", latitude),
                    "lon", String.format(Locale.ROOT, "%.6f", longitude)
            ));
            String json = http.get(uri);
            return gson.fromJson(json, OpenWeatherResponse.class);
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WeatherSdkException("Network error while fetching weather", ex);
        } catch (JsonSyntaxException ex) {
            throw new WeatherSdkException("Malformed JSON from weather provider", ex);
        }
    }
}
