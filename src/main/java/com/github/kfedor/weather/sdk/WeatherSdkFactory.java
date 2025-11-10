package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.CacheManager;
import com.github.kfedor.weather.sdk.core.GeocodingClient;
import com.github.kfedor.weather.sdk.core.PollingManager;
import com.github.kfedor.weather.sdk.core.WeatherApiClient;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.HttpClientProvider;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.registry.KeyRegistry;
import java.net.http.HttpClient;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Factory class responsible for creating and managing {@link WeatherSdk} instances.
 *
 * <p>This class ensures that only one SDK instance exists per unique API key
 * by maintaining a {@link com.github.kfedor.weather.sdk.registry.KeyRegistry} of active SDKs.
 * It also wires together all required components such as HTTP clients,
 * cache, polling, and core services.</p>
 *
 * <p>The factory abstracts all initialization logic, providing
 * safe defaults such as cache TTL, cache size, and polling interval.
 * Clients should not instantiate {@code WeatherSdk} directly.</p>
 */
public final class WeatherSdkFactory {

    private static final KeyRegistry<WeatherSdk> REGISTRY = new KeyRegistry<>();
    private static final long TTL_MILLIS = TimeUnit.MINUTES.toMillis(10);
    private static final int MAX_CACHE = 10;
    private static final long DEFAULT_POLL_SEC = 60;

    private WeatherSdkFactory() {
    }

    /**
     * Creates a new {@link WeatherSdk} instance using default polling interval.
     *
     * @param apiKey OpenWeather API key
     * @param mode   operating mode ({@code ON_DEMAND} or {@code POLLING})
     * @return a fully initialized {@link WeatherSdk} instance
     * @throws WeatherSdkException if the API key is invalid
     */
    public static WeatherSdk create(String apiKey, WeatherSdk.Mode mode) {
        return create(apiKey, mode, DEFAULT_POLL_SEC);
    }

    /**
     * Creates or retrieves an existing {@link WeatherSdk} instance for the given API key.
     *
     * <p>If an SDK with the same API key already exists in the registry,
     * this method returns the existing instance instead of creating a new one.
     * This ensures that only one SDK instance is active per key at any time.</p>
     *
     * <p>This method is synchronized to guarantee thread-safe initialization
     * in multithreaded environments.</p>
     *
     * @param apiKey      OpenWeather API key
     * @param mode        operating mode ({@code ON_DEMAND} or {@code POLLING})
     * @param pollSeconds polling interval in seconds
     * @return a fully initialized {@link WeatherSdk} instance
     * @throws WeatherSdkException if the API key is invalid or initialization fails
     */
    public static synchronized WeatherSdk create(String apiKey, WeatherSdk.Mode mode, long pollSeconds) {
        Objects.requireNonNull(apiKey);
        Objects.requireNonNull(mode);
        if (apiKey.isBlank()) {
            throw new WeatherSdkException("apiKey must not be blank");
        }

        WeatherSdk existing = REGISTRY.get(apiKey);
        if (existing != null) {
            return existing;
        }

        HttpClient httpClient = HttpClientProvider.create();
        RequestExecutor http = new RequestExecutor(httpClient);

        WeatherApiClient weatherClient = new WeatherApiClient(apiKey, http);
        GeocodingClient geocodeClient = new GeocodingClient(apiKey, http);

        CacheManager cache = new CacheManager(TTL_MILLIS, MAX_CACHE);
        WeatherService service = new WeatherService(weatherClient, geocodeClient, cache);

        PollingManager polling = new PollingManager(service, cache, Math.max(10, pollSeconds));
        WeatherSdk sdk = new WeatherSdk(apiKey, mode, service, polling);

        REGISTRY.put(apiKey, sdk);
        return sdk;
    }

    /**
     * Unregisters the specified SDK instance from the internal registry.
     *
     * <p>Called automatically by {@link WeatherSdk#destroy()} when
     * an SDK instance is being shut down or garbage-collected.
     * After unregistration, the same API key can be used to create
     * a new SDK instance if needed.</p>
     *
     * @param apiKey the API key associated with the SDK
     * @param sdk    the instance to remove from the registry
     */
    static void unregister(String apiKey, WeatherSdk sdk) {
        REGISTRY.removeIfMatches(sdk);
    }
}
