package com.github.kfedor.weather.sdk.mapper;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import com.github.kfedor.weather.sdk.model.WeatherResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeatherMapperTest {

    @Test
    void mapsAllFieldsHappyPath() {
        OpenWeatherResponse raw = new OpenWeatherResponse();

        OpenWeatherResponse.Weather w0 = new OpenWeatherResponse.Weather();
        w0.setMain("Clouds");
        w0.setDescription("scattered clouds");
        raw.setWeather(new OpenWeatherResponse.Weather[]{w0});

        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(269.6);
        main.setFeels_like(267.57);
        raw.setMain(main);

        OpenWeatherResponse.Wind wind = new OpenWeatherResponse.Wind();
        wind.setSpeed(1.38);
        raw.setWind(wind);

        raw.setVisibility(10_000);
        raw.setDt(1675744800L);

        OpenWeatherResponse.Sys sys = new OpenWeatherResponse.Sys();
        sys.setSunrise(1675751262L);
        sys.setSunset(1675787560L);
        raw.setSys(sys);

        raw.setTimezone(3600);
        raw.setName("Rome");

        WeatherResponse result = WeatherMapper.toSdk(raw);

        assertNotNull(result);
        assertNotNull(result.getWeather());
        assertEquals("Clouds", result.getWeather().getMain());
        assertEquals("scattered clouds", result.getWeather().getDescription());

        assertNotNull(result.getTemperature());
        assertEquals(269.6, result.getTemperature().getTemp(), 1e-9);
        assertEquals(267.57, result.getTemperature().getFeels_like(), 1e-9);

        assertNotNull(result.getWind());
        assertEquals(1.38, result.getWind().getSpeed(), 1e-9);

        assertEquals(10_000, result.getVisibility());
        assertEquals(1675744800L, result.getDatetime());
        assertEquals(3600, result.getTimezone());
        assertEquals("Rome", result.getName());

        assertNotNull(result.getSys());
        assertEquals(1675751262L, result.getSys().getSunrise());
        assertEquals(1675787560L, result.getSys().getSunset());
    }

    @Test
    void handlesMissingNestedBlocks_gracefully() {
        OpenWeatherResponse raw = new OpenWeatherResponse();

        raw.setVisibility(5000);
        raw.setDt(111L);
        raw.setTimezone(7200);
        raw.setName("Istanbul");

        WeatherResponse result = WeatherMapper.toSdk(raw);

        assertNull(result.getWeather());
        assertNull(result.getTemperature());
        assertNull(result.getWind());
        assertNotNull(result);
        assertEquals(5000, result.getVisibility());
        assertEquals(111L, result.getDatetime());
        assertEquals(7200, result.getTimezone());
        assertEquals("Istanbul", result.getName());
        assertNull(result.getSys());
    }

    @Test
    void handlesEmptyWeatherArray() {
        OpenWeatherResponse raw = new OpenWeatherResponse();
        raw.setWeather(new OpenWeatherResponse.Weather[0]); // пустой массив
        OpenWeatherResponse.Main main = new OpenWeatherResponse.Main();
        main.setTemp(280.0);
        main.setFeels_like(279.0);
        raw.setMain(main);

        WeatherResponse result = WeatherMapper.toSdk(raw);

        assertNull(result.getWeather());
        assertNotNull(result.getTemperature());
        assertEquals(280.0, result.getTemperature().getTemp(), 1e-9);
        assertEquals(279.0, result.getTemperature().getFeels_like(), 1e-9);
    }

    @Test
    void throwsOnNullRaw() {
        assertThrows(WeatherSdkException.class, () -> WeatherMapper.toSdk(null));
    }
}
