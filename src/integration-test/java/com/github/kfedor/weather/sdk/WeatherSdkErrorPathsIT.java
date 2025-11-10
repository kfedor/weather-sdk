package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.CacheManager;
import com.github.kfedor.weather.sdk.core.GeocodingClient;
import com.github.kfedor.weather.sdk.core.WeatherApiClient;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.util.HttpForwarding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeatherSdkErrorPathsIT {

    private WireMockServer wireMock;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());

        HttpClient client = HttpClient.newHttpClient();
        RequestExecutor http = HttpForwarding.forwarding(client, "http", "localhost", wireMock.port());

        WeatherApiClient weatherApiClient = new WeatherApiClient("BAD_KEY", http);
        GeocodingClient geocodingClient = new GeocodingClient("BAD_KEY", http);
        CacheManager cache = new CacheManager(600_000L, 10);
        service = new WeatherService(weatherApiClient, geocodingClient, cache);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void byCoordinatesUnauthorizedThrows() {
        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("10.000000"))
                .withQueryParam("lon", equalTo("20.000000"))
                .withQueryParam("appid", equalTo("BAD_KEY"))
                .willReturn(aResponse().withStatus(401).withBody("Unauthorized")));

        assertThatThrownBy(() -> service.getByCoordinates(10.0, 20.0))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("HTTP error 401 from provider");
    }

    @Test
    void byCityNotFoundResultsInCityNotFoundException() {
        stubFor(get(urlPathEqualTo("/geo/1.0/direct"))
                .withQueryParam("q", equalTo("Atlantis"))
                .withQueryParam("limit", equalTo("1"))
                .withQueryParam("appid", equalTo("BAD_KEY"))
                .willReturn(okJson("[]")));

        assertThatThrownBy(() -> service.getByCity("Atlantis"))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("City not found");
    }
}
