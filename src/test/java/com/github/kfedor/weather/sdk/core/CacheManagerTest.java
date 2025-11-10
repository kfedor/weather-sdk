package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.model.WeatherResponse;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CacheManagerTest {

    private static WeatherResponse sample(String name) {
        WeatherResponse response = new WeatherResponse();
        response.setName(name);
        return response;
    }

    @Test
    void getIfNotExpiredReturnsItemWhenFresh() {
        CacheManager cache = new CacheManager(1_000L, 10);
        String key = "paris";
        cache.put(key, sample("Paris"), RequestInfo.city("Paris"));

        CacheItem cacheItem = cache.getIfNotExpired(key);
        assertThat(cacheItem).isNotNull();
        assertThat(cacheItem.weatherResponse().getName()).isEqualTo("Paris");
        assertThat(cacheItem.requestInfo()).isNotNull();
    }

    @Test
    void getIfNotExpiredReturnsNullWhenExpired() throws Exception {
        CacheManager cache = new CacheManager(10L, 10);
        String key = "rome";
        cache.put(key, sample("Rome"), RequestInfo.city("Rome"));

        Thread.sleep(20);
        CacheItem cacheItem = cache.getIfNotExpired(key);
        assertThat(cacheItem).isNull();
    }

    @Test
    void lruEvictsLeastRecentlyUsedWhenOverCapacity() {
        CacheManager cache = new CacheManager(60_000L, 2);

        cache.put("k1", sample("K1"), RequestInfo.city("K1"));
        cache.put("k2", sample("K2"), RequestInfo.city("K2"));

        assertThat(cache.getIfNotExpired("k1")).isNotNull();

        cache.put("k3", sample("K3"), RequestInfo.city("K3"));

        assertThat(cache.getIfNotExpired("k1")).isNotNull();
        assertThat(cache.getIfNotExpired("k3")).isNotNull();
        assertThat(cache.getIfNotExpired("k2")).isNull();
    }

    @Test
    void snapshotRequestsReturnsCopyForPolling() {
        CacheManager cache = new CacheManager(60_000L, 10);
        cache.put("city:tokyo", sample("Tokyo"), RequestInfo.city("Tokyo"));
        cache.put("coordinates:1", sample("Coordinates"), RequestInfo.coordinates(59.9, 30.3));

        Map<String, RequestInfo> snapshotRequests = cache.snapshotRequests();

        assertThat(snapshotRequests).hasSize(2);
        assertThat(snapshotRequests.keySet()).containsExactlyInAnyOrder("city:tokyo", "coordinates:1");

        RequestInfo cityInfo = snapshotRequests.get("city:tokyo");
        assertThat(cityInfo).isNotNull();
        assertThat(cityInfo.city()).isEqualTo("Tokyo");

        RequestInfo requestInfo = snapshotRequests.get("coordinates:1");
        assertThat(requestInfo).isNotNull();
        assertThat(requestInfo.latitude()).isCloseTo(59.9, within(1e-9));
        assertThat(requestInfo.longitude()).isCloseTo(30.3, within(1e-9));
    }

    @Test
    void clearRemovesAllEntries() {
        CacheManager cache = new CacheManager(60_000L, 10);
        cache.put("a", sample("A"), RequestInfo.city("A"));
        cache.put("b", sample("B"), RequestInfo.city("B"));

        cache.clear();

        assertThat(cache.getIfNotExpired("a")).isNull();
        assertThat(cache.getIfNotExpired("b")).isNull();
        assertThat(cache.snapshotRequests()).isEmpty();
    }

    private static org.assertj.core.data.Offset<Double> within(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
