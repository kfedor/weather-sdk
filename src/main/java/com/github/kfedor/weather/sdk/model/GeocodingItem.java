package com.github.kfedor.weather.sdk.model;

import com.google.gson.annotations.SerializedName;

/**
 * Represents a single location entry returned by the OpenWeather Geocoding API.
 *
 * <p>Each item contains only the essential geographic data — latitude and longitude —
 * which are used by the SDK to request weather information for a given city.</p>
 *
 * <p>This class is a simple data holder (POJO) used internally by
 * {@link com.github.kfedor.weather.sdk.core.GeocodingClient}.</p>
 *
 * @param latitude latitude of the location
 * @param longitude longitude of the location
 */
public record GeocodingItem(@SerializedName("lat") double latitude, @SerializedName("lon") double longitude) {
}