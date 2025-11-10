package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.PollingManager;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.model.WeatherResponse;

/**
 * Public entry point of the Weather SDK.
 *
 * <p>This class provides a simplified interface for developers
 * to access current weather data via the OpenWeather API.
 * Instances are created through {@link WeatherSdkFactory#create(String, Mode)}.</p>
 *
 * <p>Depending on the {@link Mode}, the SDK either:
 * <ul>
 *   <li>fetches data on demand (lazy requests)</li>
 *   <li>or keeps data continuously refreshed in the background (polling)</li>
 * </ul>
 * </p>
 *
 * <p>This class is thread-safe and designed to be used as a long-lived
 * singleton within an application.</p>
 *
 * @see WeatherSdkFactory
 * @see com.github.kfedor.weather.sdk.model.WeatherResponse
 */
public final class WeatherSdk {

    /**
     * Defines how the SDK operates and updates weather data.
     *
     * <ul>
     *   <li>{@code ON_DEMAND} — data is fetched only when requested.</li>
     *   <li>{@code POLLING} — data is refreshed automatically
     *       in the background at a fixed interval.</li>
     * </ul>
     */
    public enum Mode {ON_DEMAND, POLLING}

    private final String apiKey;
    private final Mode mode;
    private final WeatherService service;
    private final PollingManager polling;

    WeatherSdk(String apiKey, Mode mode, WeatherService service, PollingManager polling) {
        this.apiKey = apiKey;
        this.mode = mode;
        this.service = service;
        this.polling = polling;
        if (mode == Mode.POLLING) this.polling.start();
    }

    /**
     * Retrieves current weather data for the specified city.
     *
     * <p>If the data for this city is present and still valid in cache,
     * it will be returned immediately.
     * Otherwise, the SDK queries the OpenWeather API via the geocoding service
     * to resolve the city coordinates, then fetches live weather data.</p>
     *
     * @param city the city name (case-insensitive)
     * @return a {@link com.github.kfedor.weather.sdk.model.WeatherResponse} with current weather information
     */
    public WeatherResponse getCurrentByCity(String city) {
        return service.getByCity(city);
    }

    /**
     * Retrieves current weather data by geographic coordinates.
     *
     * <p>If the data for these coordinates is present and valid in cache,
     * it will be returned from memory; otherwise, the SDK requests
     * live data directly from the OpenWeather API.</p>
     *
     * @param latitude  of the city
     * @param longitude of the city
     * @return a {@link com.github.kfedor.weather.sdk.model.WeatherResponse} with current weather information
     */
    public WeatherResponse getCurrentByCoordinates(double latitude, double longitude) {
        return service.getByCoordinates(latitude, longitude);
    }

    /**
     * Gracefully shuts down the SDK instance.
     *
     * <p>Stops background polling (if enabled), clears cache,
     * and unregisters this SDK instance from the internal registry.
     * After calling this method, the instance should no longer be used.</p>
     */
    public void destroy() {
        polling.stop();
        service.clear();
        WeatherSdkFactory.unregister(apiKey, this);
    }
}
