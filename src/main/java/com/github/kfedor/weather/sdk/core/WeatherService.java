package com.github.kfedor.weather.sdk.core;


import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.mapper.WeatherMapper;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import com.github.kfedor.weather.sdk.model.WeatherResponse;
import java.util.Objects;
import java.util.Optional;

/**
 * Core business layer of the Weather SDK.
 *
 * <p>Handles caching, API calls, and data transformation.
 * This class coordinates between {@link WeatherApiClient},
 * {@link GeocodingClient}, and {@link CacheManager} to provide
 * a unified interface for fetching weather data.</p>
 *
 * <p>It is used internally by {@link com.github.kfedor.weather.sdk.WeatherSdk}
 * and is not intended for direct use by SDK clients.</p>
 */
public class WeatherService {

    private final WeatherApiClient weather;
    private final GeocodingClient geocode;
    private final CacheManager cache;

    public WeatherService(WeatherApiClient weather, GeocodingClient geocode, CacheManager cache) {
        this.weather = weather;
        this.geocode = geocode;
        this.cache = cache;
    }

    /**
     * Returns current weather data for the given city.
     *
     * <p>Uses cache if available; otherwise resolves coordinates via
     * {@link GeocodingClient} and fetches fresh data from the weather API.
     * Updates the cache with the latest result.</p>
     *
     * @param cityName target city name
     * @return a fully populated {@link com.github.kfedor.weather.sdk.model.WeatherResponse}
     * @throws WeatherSdkException if city not found or API call fails
     */
    public WeatherResponse getByCity(String cityName) {
        if (cityName == null || cityName.isBlank()) {
            throw new WeatherSdkException("City must not be empty");
        }
        String key = Keys.formatCity(cityName);
        CacheItem cacheItem = cache.getIfNotExpired(key);
        if (cacheItem != null) {
            return cacheItem.weatherResponse();
        }

        GeocodingClient.Location location = geocode.findFirstLocation(cityName)
                .orElseThrow(() -> new WeatherSdkException("City not found: " + cityName));
        WeatherResponse response = map(weather.byCoordinates(location.latitude(), location.longitude()));
        cache.put(key, response, RequestInfo.city(cityName));
        return response;
    }

    /**
     * Returns current weather data by coordinates.
     *
     * <p>Checks the cache first; if data is missing or expired,
     * requests new data from {@link WeatherApiClient} and updates the cache.</p>
     *
     * @param latitude  of the city
     * @param longitude of the city
     * @return a {@link com.github.kfedor.weather.sdk.model.WeatherResponse}
     * @throws WeatherSdkException if API call fails
     */
    public WeatherResponse getByCoordinates(double latitude, double longitude) {
        String key = Keys.coordinates(latitude, longitude);
        CacheItem cacheItem = cache.getIfNotExpired(key);
        if (cacheItem != null) {
            return cacheItem.weatherResponse();
        }

        WeatherResponse response = map(weather.byCoordinates(latitude, longitude));
        cache.put(key, response, RequestInfo.coordinates(latitude, longitude));
        return response;
    }

    /**
     * Refreshes a single cached entry by re-fetching its data from the API.
     *
     * <p>Used internally by the polling mechanism to update weather data
     * without explicit user requests. The refreshed result replaces the
     * old cache entry for the given key.</p>
     *
     * @param info     request metadata (city or coordinates)
     * @param cacheKey key of the cache entry to refresh
     */
    public void refresh(RequestInfo info, String cacheKey) {
        if (info.type() == RequestInfo.Type.CITY) {
            Optional<GeocodingClient.Location> location = geocode.findFirstLocation(info.city());
            location.ifPresent(loc ->
                    cache.put(cacheKey, map(weather.byCoordinates(loc.latitude(), loc.longitude())), info));
        } else {
            cache.put(cacheKey, map(weather.byCoordinates(info.latitude(), info.longitude())), info);
        }
    }

    /**
     * Clears all entries from the internal cache.
     * <p>Typically called when the SDK is destroyed or reset.</p>
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Converts a raw OpenWeather response into a standard SDK model.
     *
     * <p>Delegates to {@link com.github.kfedor.weather.sdk.mapper.WeatherMapper}
     * for data transformation.</p>
     *
     * @param openWeatherResponse raw provider response
     * @return simplified weather model
     */
    private WeatherResponse map(OpenWeatherResponse openWeatherResponse) {
        return WeatherMapper.toSdk(Objects.requireNonNull(openWeatherResponse));
    }

    /**
     * Utility class for generating consistent cache keys.
     *
     * <p>Used internally to normalize city names and
     * coordinates into canonical string formats.</p>
     */
    static final class Keys {

        /**
         * Normalizes a city name to lowercase and trims extra spaces.
         */
        static String formatCity(String cityName) {
            return cityName.trim().toLowerCase();
        }

        /**
         * Formats coordinates into a canonical key string, e.g.
         * {@code latitude=59.934280;longitude=30.335099}.
         */
        static String coordinates(double latitude, double longitude) {
            return String.format("latitude=%.6f;longitude=%.6f", latitude, longitude);
        }
    }
}
