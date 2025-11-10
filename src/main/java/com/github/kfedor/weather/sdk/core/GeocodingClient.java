package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.HttpConfig;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.http.UrlBuilder;
import com.github.kfedor.weather.sdk.model.GeocodingItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Optional;

/**
 * Low-level HTTP client for the OpenWeather Geocoding API.
 *
 * <p>This client resolves city names into geographic coordinates
 * (latitude and longitude) that can later be used to request
 * actual weather data from the weather API.</p>
 *
 * <p>Each request returns at most one matching location, but
 * the API itself can return multiple results — this client
 * only takes the first one for simplicity.</p>
 *
 * <p>Intended for internal SDK use.</p>
 */
public class GeocodingClient {
    private final String apiKey;
    private final RequestExecutor http;
    private final Gson gson = new Gson();

    public record Location(double latitude, double longitude) {
    }

    public GeocodingClient(String apiKey, RequestExecutor http) {
        this.apiKey = apiKey;
        this.http = http;
    }

    /**
     * Resolves the specified city name into geographic coordinates.
     *
     * <p>Performs a request to the OpenWeather Geocoding API using the
     * {@code /geo/1.0/direct} endpoint with {@code limit=1}. If at least
     * one location is found, returns its latitude and longitude.</p>
     *
     * @param city the city name to resolve
     * @return an {@link Optional} containing the first matching location, or empty if not found
     * @throws com.github.kfedor.weather.sdk.exception.WeatherSdkException if a network error occurs or the API returns malformed data
     */
    public Optional<Location> findFirstLocation(String city) {
        try {
            URI uri = UrlBuilder.build(HttpConfig.GEOCODING_ENDPOINT, Map.of(
                    "appid", apiKey, "q", city, "limit", "1"
            ));
            String json = http.get(uri);
            GeocodingItem[] items = gson.fromJson(json, GeocodingItem[].class);
            if (items == null || items.length == 0) {
                return Optional.empty();
            }
            return Optional.of(new Location(items[0].latitude(), items[0].longitude()));
        } catch (IOException | InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new WeatherSdkException("Network error during geocoding", ex);
        } catch (JsonSyntaxException ex) {
            throw new WeatherSdkException("Malformed JSON from geocoding provider", ex);
        }
    }
}
