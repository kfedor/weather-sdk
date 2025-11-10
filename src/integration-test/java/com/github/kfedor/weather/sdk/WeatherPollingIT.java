package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.CacheManager;
import com.github.kfedor.weather.sdk.core.GeocodingClient;
import com.github.kfedor.weather.sdk.core.PollingManager;
import com.github.kfedor.weather.sdk.core.RequestInfo;
import com.github.kfedor.weather.sdk.core.WeatherApiClient;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.util.HttpForwarding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

class WeatherPollingIT {

    private WireMockServer wireMock;
    private CacheManager cache;
    private WeatherService service;
    private PollingManager polling;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());

        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("London"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo("TEST_KEY"))
                .willReturn(okJson("[{\"lat\":51.507400,\"lon\":-0.127800}]")));

        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("51.507400"))
                .withQueryParam("lon", equalTo("-0.127800"))
                .withQueryParam("appid", equalTo("TEST_KEY"))
                .willReturn(okJson("""
                        {
                          "weather":[{"main":"Clear","description":"clear sky"}],
                          "main":{"temp":290.00,"feels_like":289.00},
                          "wind":{"speed":2.00},
                          "visibility":9000,
                          "dt":200,
                          "sys":{"sunrise":100,"sunset":300},
                          "timezone":0,
                          "name":"London"
                        }
                        """)));

        HttpClient client = HttpClient.newHttpClient();
        RequestExecutor http = HttpForwarding.forwarding(client, "http", "localhost", wireMock.port());

        WeatherApiClient weatherApiClient = new WeatherApiClient("TEST_KEY", http);
        GeocodingClient geocodingClient = new GeocodingClient("TEST_KEY", http);
        cache = new CacheManager(600_000L, 10);
        service = new WeatherService(weatherApiClient, geocodingClient, cache);

        cache.put("london", new com.github.kfedor.weather.sdk.model.WeatherResponse(), RequestInfo.city("London"));

        polling = new PollingManager(service, cache, 1L);
    }

    @AfterEach
    void tearDown() {
        polling.stop();
        wireMock.stop();
    }

    @Test
    void pollingRefreshesCacheInBackground() throws Exception {
        polling.start();

        Thread.sleep(1500);

        verify(getRequestedFor(urlPathEqualTo("/geo/1.0/direct")));
        verify(getRequestedFor(urlPathEqualTo("/data/2.5/weather")));

        var entry = cache.getIfNotExpired("london");
        assertThat(entry).isNotNull();
        assertThat(entry.requestInfo().type()).isEqualTo(RequestInfo.Type.CITY);
    }
}
