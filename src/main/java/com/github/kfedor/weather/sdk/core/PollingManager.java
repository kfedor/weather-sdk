package com.github.kfedor.weather.sdk.core;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Manages background polling of weather data.
 *
 * <p>Used in {@link com.github.kfedor.weather.sdk.WeatherSdk.Mode#POLLING} mode
 * to periodically refresh all cached weather entries without user interaction.</p>
 *
 * <p>The polling runs in a dedicated daemon thread and updates
 * each cache entry based on its associated {@link RequestInfo}.
 * All operations are thread-safe and designed to fail silently
 * in case of transient network errors.</p>
 */
public class PollingManager {
    private final WeatherService service;
    private final CacheManager cache;
    private final long intervalSec;
    private ScheduledExecutorService scheduler;

    public PollingManager(WeatherService service, CacheManager cache, long intervalSec) {
        this.service = service;
        this.cache = cache;
        this.intervalSec = intervalSec;
    }

    /**
     * Starts the background polling scheduler if it is not already running.
     *
     * <p>Creates a single-threaded scheduled executor that periodically
     * triggers weather data refresh for all cached locations.
     * The interval is defined at SDK initialization.</p>
     *
     * <p>This method is idempotent — calling it multiple times
     * has no effect once the scheduler is active.</p>
     */
    public void start() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "weather-sdk-polling");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleAtFixedRate(this::tick, intervalSec, intervalSec, TimeUnit.SECONDS);
    }

    /**
     * Stops the background polling process and releases related resources.
     *
     * <p>After stopping, polling can be restarted later
     * by calling {@link #start()} again if needed.</p>
     */
    public void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    /**
     * Executes a single polling cycle.
     *
     * <p>This method collects a snapshot of all cache entries
     * and re-fetches their data from the API through
     * {@link WeatherService#refresh(RequestInfo, String)}.</p>
     *
     * <p>It is typically called automatically by the scheduler
     * at fixed intervals, but may also be invoked manually for testing.</p>
     */
    private void tick() {
        try {
            Map<String, RequestInfo> snapshot = cache.snapshotRequests();
            snapshot.forEach((key, info) -> {
                try {
                    service.refresh(info, key);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }
}