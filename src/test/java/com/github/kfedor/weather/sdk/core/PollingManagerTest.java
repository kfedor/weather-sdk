package com.github.kfedor.weather.sdk.core;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class PollingManagerTest {

    private WeatherService weatherService;
    private CacheManager cacheManager;
    private PollingManager pollingManager;

    @BeforeEach
    void setUp() {
        weatherService = mock(WeatherService.class);
        cacheManager = mock(CacheManager.class);
        pollingManager = new PollingManager(weatherService, cacheManager, 1L);
    }

    @Test
    void tickRefreshesAllEntriesFromSnapshot() throws Exception {
        Map<String, RequestInfo> snapshot = new LinkedHashMap<>();
        snapshot.put("helsinki", RequestInfo.city("Helsinki"));
        snapshot.put("coordinates:59.934280;30.335099", RequestInfo.coordinates(59.934280, 30.335099));

        when(cacheManager.snapshotRequests()).thenReturn(snapshot);

        Method tick = PollingManager.class.getDeclaredMethod("tick");
        tick.setAccessible(true);
        tick.invoke(pollingManager);

        verify(weatherService, times(1)).refresh(RequestInfo.city("Helsinki"), "helsinki");
        verify(weatherService, times(1)).refresh(
                RequestInfo.coordinates(59.934280, 30.335099),
                "coordinates:59.934280;30.335099"
        );
        verifyNoMoreInteractions(weatherService);
    }

    @Test
    void startIsIdempotentAndStopDoesNotThrow() {
        assertThatCode(() -> {
            pollingManager.start();
            pollingManager.start();
            pollingManager.stop();
            pollingManager.stop();
        }).doesNotThrowAnyException();
    }
}
