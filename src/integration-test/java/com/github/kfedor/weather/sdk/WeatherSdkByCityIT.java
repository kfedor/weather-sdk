package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.CacheManager;
import com.github.kfedor.weather.sdk.core.GeocodingClient;
import com.github.kfedor.weather.sdk.core.WeatherApiClient;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.model.WeatherResponse;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration: by city → geocoding (limit=1) → weather by coordinates → cache hit on second call.
 * Uses WireMock; RequestExecutor подменяет host на WireMock, сохраняя path/query.
 */
class WeatherSdkByCityIT {

    private WireMockServer wireMock;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        WireMock.configureFor("localhost", wireMock.port());

        wireMock.stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParams(Map.of(
                        "q", equalTo("Helsinki"),
                        "limit", equalTo("1"),
                        "appid", equalTo("TEST_KEY")
                ))
                .willReturn(okJson("[{\"lat\":60.1699,\"lon\":24.9384}]")));

        wireMock.stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParams(Map.of(
                        "lat", equalTo("60.169900"),
                        "lon", equalTo("24.938400"),
                        "appid", equalTo("TEST_KEY")
                ))
                .willReturn(okJson("""
                        {
                          "weather":[{"main":"Clouds","description":"broken clouds"}],
                          "main":{"temp":280.15,"feels_like":278.50},
                          "wind":{"speed":3.60},
                          "visibility":10000,
                          "dt":1675744800,
                          "sys":{"sunrise":1675751262,"sunset":1675787560},
                          "timezone":7200,
                          "name":"Helsinki"
                        }
                        """)));

        HttpClient client = HttpClient.newHttpClient();
        RequestExecutor forwarding = new ForwardingRequestExecutor(client, "http", "localhost", wireMock.port());

        WeatherApiClient weatherApiClient = new WeatherApiClient("TEST_KEY", forwarding);
        GeocodingClient geocodingClient = new GeocodingClient("TEST_KEY", forwarding);

        CacheManager cache = new CacheManager(600_000L, 10);
        service = new WeatherService(weatherApiClient, geocodingClient, cache);
    }

    @AfterEach
    void tearDown() {
        if (wireMock != null) wireMock.stop();
    }

    @Test
    void byCityEndToEndPopulatesCacheAndSecondCallHitsCache() {
        WeatherResponse first = service.getByCity("Helsinki");
        assertThat(first.getName()).isEqualTo("Helsinki");
        assertThat(first.getTemperature()).isNotNull();

        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/geo/1.0/direct")));
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/data/2.5/weather")));

        WeatherResponse second = service.getByCity("Helsinki");
        assertThat(second.getName()).isEqualTo("Helsinki");

        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/geo/1.0/direct")));
        wireMock.verify(1, getRequestedFor(urlPathEqualTo("/data/2.5/weather")));
    }

    static class ForwardingRequestExecutor extends RequestExecutor {
        private final HttpClient client;
        private final String scheme;
        private final String host;
        private final int port;

        ForwardingRequestExecutor(HttpClient client, String scheme, String host, int port) {
            super(client);
            this.client = client;
            this.scheme = scheme;
            this.host = host;
            this.port = port;
        }

        @Override
        public String get(URI original) throws IOException, InterruptedException {
            URI rewritten = rewrite(original);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(rewritten)
                    .header("Accept", "application/json")
                    .GET().build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            if (code >= 200 && code < 300) return resp.body();
            throw new RuntimeException("HTTP error from WireMock: " + code);
        }

        private URI rewrite(URI original) {
            String path = original.getPath();
            String query = original.getQuery();
            String newUri = scheme + "://" + host + ":" + port + path + (query == null ? "" : "?" + query);
            return URI.create(newUri);
        }
    }
}
