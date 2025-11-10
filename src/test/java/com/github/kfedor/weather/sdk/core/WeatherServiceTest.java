package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import com.github.kfedor.weather.sdk.model.WeatherResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class WeatherServiceTest {

    private WeatherApiClient weatherApiClient;
    private GeocodingClient geocodingClient;
    private CacheManager cacheManager;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        weatherApiClient = mock(WeatherApiClient.class);
        geocodingClient = mock(GeocodingClient.class);
        cacheManager = spy(new CacheManager(60_000L, 3));
        service = new WeatherService(weatherApiClient, geocodingClient, cacheManager);
    }

    private static OpenWeatherResponse raw(String city, double temp, double feels) {
        OpenWeatherResponse raw = new OpenWeatherResponse();

        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(temp);
        main.setFeels_like(feels);
        raw.setMain(main);

        raw.setName(city);
        raw.setTimezone(3600);
        raw.setDt(111L);

        return raw;
    }

    private static WeatherResponse anyMapped(String name) {
        WeatherResponse weatherResponse = new WeatherResponse();
        weatherResponse.setName(name);
        WeatherResponse.Temperature t = new WeatherResponse.Temperature();
        t.setTemp(280.0);
        t.setFeels_like(279.0);
        weatherResponse.setTemperature(t);
        return weatherResponse;
    }

    /**
     * Happy path: by city -> cache miss -> geocoding resolves -> weather fetched -> cached and returned.
     */
    @Test
    void getByCityResolvesCoordinatesFetchesWeatherAndCaches() {
        when(geocodingClient.findFirstLocation("Helsinki"))
                .thenReturn(Optional.of(new GeocodingClient.Location(60.1699, 24.9384)));

        when(weatherApiClient.byCoordinates(60.1699, 24.9384))
                .thenReturn(raw("Helsinki", 271.0, 270.0));

        WeatherResponse out = service.getByCity("Helsinki");

        assertThat(out).isNotNull();
        assertThat(out.getName()).isEqualTo("Helsinki");
        assertThat(out.getTemperature()).isNotNull();

        CacheItem cached = cacheManager.getIfNotExpired("helsinki");
        assertThat(cached).isNotNull();
        assertThat(cached.weatherResponse().getName()).isEqualTo("Helsinki");

        verify(geocodingClient, times(1)).findFirstLocation("Helsinki");
        verify(weatherApiClient, times(1)).byCoordinates(60.1699, 24.9384);
    }

    /**
     * Second call with same city should hit cache (no new network calls).
     */
    @Test
    void getByCityUsesCacheOnSecondCall() {
        cacheManager.put("paris", anyMapped("Paris"), RequestInfo.city("Paris"));

        WeatherResponse out = service.getByCity("Paris");
        assertThat(out.getName()).isEqualTo("Paris");

        verifyNoInteractions(geocodingClient, weatherApiClient);
    }

    /**
     * City not found by geocoding -> exception thrown.
     */
    @Test
    void getByCityThrowsWhenCityNotFound() {
        when(geocodingClient.findFirstLocation("Atlantis")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCity("Atlantis"))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("City not found");

        verify(geocodingClient, times(1)).findFirstLocation("Atlantis");
        verifyNoInteractions(weatherApiClient);
    }

    /**
     * Happy path by coordinates -> fetch weather and cache using coordinates key.
     */
    @Test
    void getByCoordinatesFetchesWeather_andCaches() {
        double latitude = 59.934280;
        double longitude = 30.335099;

        when(weatherApiClient.byCoordinates(latitude, longitude))
                .thenReturn(raw("Saint Petersburg", 270.0, 268.0));

        WeatherResponse out = service.getByCoordinates(latitude, longitude);

        assertThat(out).isNotNull();
        assertThat(out.getName()).isEqualTo("Saint Petersburg");

        String expectedKey = WeatherService.Keys.coordinates(latitude, longitude);
        CacheItem cached = cacheManager.getIfNotExpired(expectedKey);
        assertThat(cached).isNotNull();
        assertThat(cached.weatherResponse().getName()).isEqualTo("Saint Petersburg");
    }

    /**
     * refresh: CITY path -> re-resolve coordinates -> re-fetch weather -> cache updated under same key.
     */
    @Test
    void refreshUpdatesCachedEntryForCityRequest() {
        String cacheKey = "london";
        cacheManager.put(cacheKey, anyMapped("OldLondon"), RequestInfo.city("London"));

        when(geocodingClient.findFirstLocation("London"))
                .thenReturn(Optional.of(new GeocodingClient.Location(51.5074, -0.1278)));
        when(weatherApiClient.byCoordinates(51.5074, -0.1278))
                .thenReturn(raw("London", 281.0, 279.0));

        service.refresh(RequestInfo.city("London"), cacheKey);

        CacheItem updated = cacheManager.getIfNotExpired(cacheKey);
        assertThat(updated).isNotNull();
        assertThat(updated.weatherResponse().getName()).isEqualTo("London");
    }

    /**
     * refresh: COORDINATES path -> direct byCoordinates() without geocoding.
     */
    @Test
    void refreshUpdatesCachedEntryForCoordinatesRequest() {
        String cacheKey = WeatherService.Keys.coordinates(35.0, 139.0);
        cacheManager.put(cacheKey, anyMapped("OldTokyo"), RequestInfo.coordinates(35.0, 139.0));

        when(weatherApiClient.byCoordinates(35.0, 139.0))
                .thenReturn(raw("Tokyo", 285.0, 283.0));

        service.refresh(RequestInfo.coordinates(35.0, 139.0), cacheKey);

        CacheItem updated = cacheManager.getIfNotExpired(cacheKey);
        assertThat(updated).isNotNull();
        assertThat(updated.weatherResponse().getName()).isEqualTo("Tokyo");
    }

    /**
     * clear: removes everything from cache.
     */
    @Test
    void clear_removesAllCachedEntries() {
        cacheManager.put("rome", anyMapped("Rome"), RequestInfo.city("Rome"));
        cacheManager.put("madrid", anyMapped("Madrid"), RequestInfo.city("Madrid"));

        service.clear();

        assertThat(cacheManager.getIfNotExpired("rome")).isNull();
        assertThat(cacheManager.getIfNotExpired("madrid")).isNull();
        assertThat(cacheManager.snapshotRequests()).isEmpty();
    }

    /**
     * Verifies that keys are normalized for city names.
     */
    @Test
    void keysNormalization_isLowerCaseAndTrimmed() {
        assertThat(WeatherService.Keys.formatCity("  New York  ")).isEqualTo("new york");
    }
}
