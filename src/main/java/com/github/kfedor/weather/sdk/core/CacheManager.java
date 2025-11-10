package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.model.WeatherResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * In-memory cache for storing recently fetched weather data.
 *
 * <p>Implements a simple TTL (time-to-live) and LRU (least-recently-used)
 * eviction policy to limit memory usage while keeping frequently
 * accessed entries available for fast reads.</p>
 *
 * <p>The cache stores up to a fixed number of entries and automatically
 * discards the least recently used one when the limit is exceeded.
 * Each entry expires after the configured TTL.</p>
 *
 * <p>Thread-safe: all operations are synchronized.</p>
 */
public class CacheManager {
    private final long ttlMillis;
    private final int maxSize;

    private final LinkedHashMap<String, CacheItem> leastRecentlyUsed =
            new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, CacheItem> eldest) {
                    return size() > CacheManager.this.maxSize;
                }
            };

    public CacheManager(long ttlMillis, int maxSize) {
        this.ttlMillis = ttlMillis;
        this.maxSize = maxSize;
    }

    /**
     * Returns a cached entry if it exists and has not expired yet.
     *
     * <p>The method checks the entry’s {@code lastUpdated} timestamp against
     * the configured TTL and returns it only if still valid.</p>
     *
     * @param key unique cache key
     * @return the cached {@link CacheItem} if present and valid; otherwise {@code null}
     */
    public synchronized CacheItem getIfNotExpired(String key) {
        CacheItem cacheItem = leastRecentlyUsed.get(key);
        if (cacheItem == null) {
            return null;
        }
        return (System.currentTimeMillis() - cacheItem.lastUpdated()) < ttlMillis ? cacheItem : null;
    }

    /**
     * Adds or updates an entry in the cache.
     *
     * <p>If the cache size exceeds its maximum capacity, the least recently used
     * entry is automatically evicted. Each stored entry includes the time it was
     * written and the {@link RequestInfo} needed to refresh it later.</p>
     *
     * @param key             unique cache key
     * @param weatherResponse weather data to store
     * @param requestInfo     information for refreshing this entry
     */
    public synchronized void put(String key, WeatherResponse weatherResponse, RequestInfo requestInfo) {
        leastRecentlyUsed.put(key, new CacheItem(weatherResponse, System.currentTimeMillis(), requestInfo));
    }

    /**
     * Creates a snapshot of all active cache entries.
     *
     * <p>Returns a copy of all current keys and their corresponding
     * {@link RequestInfo} objects. Used by the polling mechanism to refresh
     * cached data without modifying the cache during iteration.</p>
     *
     * @return a copy of current cache entries mapped to their {@link RequestInfo}
     */
    public synchronized Map<String, RequestInfo> snapshotRequests() {
        Map<String, RequestInfo> copy = new LinkedHashMap<>();
        leastRecentlyUsed.forEach((key, value) -> copy.put(key, value.requestInfo()));
        return copy;
    }

    /**
     * Removes all entries from the cache.
     *
     * <p>Typically invoked when the SDK is shut down or reset.</p>
     */
    public synchronized void clear() {
        leastRecentlyUsed.clear();
    }
}
