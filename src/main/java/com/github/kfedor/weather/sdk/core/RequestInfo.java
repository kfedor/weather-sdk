package com.github.kfedor.weather.sdk.core;

/**
 * Describes how a cached weather entry can be retrieved or refreshed.
 *
 * <p>Each instance specifies whether the request is based on
 * a city name or geographic coordinates, along with the data
 * needed to perform that request again.</p>
 *
 * <p>This record is primarily used by the caching and polling
 * mechanisms inside the SDK.</p>
 */
public record RequestInfo(Type type, String city, double latitude, double longitude) {

    /**
     * Defines the type of weather request.
     *
     * <ul>
     *   <li>{@code CITY} — weather request identified by city name.</li>
     *   <li>{@code COORDINATES} — weather request identified by latitude and longitude.</li>
     * </ul>
     */
    public enum Type {CITY, COORDINATES}

    /**
     * Creates a {@code RequestInfo} describing a request
     * for weather data by city name.
     *
     * @param city the city name
     * @return a new {@code RequestInfo} instance of type {@link Type#CITY}
     */
    public static RequestInfo city(String city) {
        return new RequestInfo(Type.CITY, city, 0, 0);
    }

    /**
     * Creates a {@code RequestInfo} describing a request
     * for weather data by geographic coordinates.
     *
     * @param latitude  latitude value
     * @param longitude longitude value
     * @return a new {@code RequestInfo} instance of type {@link Type#COORDINATES}
     */
    public static RequestInfo coordinates(double latitude, double longitude) {
        return new RequestInfo(Type.COORDINATES, null, latitude, longitude);
    }
}
