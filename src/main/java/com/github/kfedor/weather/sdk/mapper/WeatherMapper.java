package com.github.kfedor.weather.sdk.mapper;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import com.github.kfedor.weather.sdk.model.WeatherResponse;

import static com.github.kfedor.weather.sdk.model.WeatherResponse.*;

/**
 * Converts raw OpenWeather API responses into the SDK's standardized format.
 *
 * <p>This class isolates all transformation logic between the provider's
 * response model ({@link com.github.kfedor.weather.sdk.model.OpenWeatherResponse})
 * and the SDK's own model ({@link com.github.kfedor.weather.sdk.model.WeatherResponse}).</p>
 *
 * <p>It ensures that only relevant fields are extracted and mapped,
 * maintaining a consistent and provider-agnostic data contract for SDK users.</p>
 *
 * <p>Intended for internal use by the SDK service layer.</p>
 *
 * @see com.github.kfedor.weather.sdk.model.OpenWeatherResponse
 * @see com.github.kfedor.weather.sdk.model.WeatherResponse
 */
public final class WeatherMapper {

    private WeatherMapper() {
    }

    /**
     * Converts a raw {@link com.github.kfedor.weather.sdk.model.OpenWeatherResponse}
     * object into a {@link com.github.kfedor.weather.sdk.model.WeatherResponse}.
     *
     * <p>Extracts essential information such as temperature, weather description,
     * wind, visibility, timestamps, and city name. Any missing or unexpected
     * fields in the raw data are handled gracefully.</p>
     *
     * @param openWeatherResponse the raw API response to convert
     * @return a simplified, provider-independent {@code WeatherResponse}
     * @throws com.github.kfedor.weather.sdk.exception.WeatherSdkException if required fields are missing or data is invalid
     */
    public static WeatherResponse toSdk(OpenWeatherResponse openWeatherResponse) {
        if (openWeatherResponse == null) {
            throw new WeatherSdkException("Empty provider response");
        }
        WeatherResponse responseToClient = new WeatherResponse();

        if (openWeatherResponse.getWeather() != null && openWeatherResponse.getWeather().length > 0) {
            Weather weather = new Weather();
            weather.setMain(openWeatherResponse.getWeather()[0].getMain());
            weather.setDescription(openWeatherResponse.getWeather()[0].getDescription());
            responseToClient.setWeather(weather);
        }
        if (openWeatherResponse.getMain() != null) {
            Temperature temperature = new Temperature();
            temperature.setTemp(openWeatherResponse.getMain().getTemp());
            temperature.setFeels_like(openWeatherResponse.getMain().getFeels_like());
            responseToClient.setTemperature(temperature);
        }
        responseToClient.setVisibility(openWeatherResponse.getVisibility());
        if (openWeatherResponse.getWind() != null) {
            Wind wind = new Wind();
            wind.setSpeed(openWeatherResponse.getWind().getSpeed());
            responseToClient.setWind(wind);
        }
        responseToClient.setDatetime(openWeatherResponse.getDt());
        if (openWeatherResponse.getSys() != null) {
            Sys sys = new Sys();
            sys.setSunrise(openWeatherResponse.getSys().getSunrise());
            sys.setSunset(openWeatherResponse.getSys().getSunset());
            responseToClient.setSys(sys);
        }
        responseToClient.setTimezone(openWeatherResponse.getTimezone());
        responseToClient.setName(openWeatherResponse.getName());
        return responseToClient;
    }
}