package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.model.WeatherResponse;

/**
 * Represents a single cached weather entry.
 *
 * <p>Each cache item contains the latest weather data, the timestamp
 * when it was last updated, and the {@link RequestInfo} describing how
 * this data can be refreshed.</p>
 *
 * <p>Instances of this record are immutable and thread-safe.</p>
 *
 * @param weatherResponse cached weather data
 * @param lastUpdated     timestamp in milliseconds when the data was stored
 * @param requestInfo     information required to refresh this entry
 */
public record CacheItem(WeatherResponse weatherResponse, long lastUpdated, RequestInfo requestInfo) {
}
