package com.github.kfedor.weather.sdk;

import com.github.kfedor.weather.sdk.core.CacheManager;
import com.github.kfedor.weather.sdk.core.GeocodingClient;
import com.github.kfedor.weather.sdk.core.WeatherApiClient;
import com.github.kfedor.weather.sdk.core.WeatherService;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.model.WeatherResponse;
import com.github.kfedor.weather.sdk.util.HttpForwarding;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

class WeatherSdkByCoordinatesIT {

    private WireMockServer wireMock;
    private WeatherService service;

    @BeforeEach
    void setUp() {
        wireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        wireMock.start();
        configureFor("localhost", wireMock.port());
        
        stubFor(get(urlPathEqualTo("/data/2.5/weather"))
                .withQueryParam("lat", equalTo("59.934280"))
                .withQueryParam("lon", equalTo("30.335099"))
                .withQueryParam("appid", equalTo("TEST_KEY"))
                .willReturn(okJson("""
                        {
                          "weather":[{"main":"Clouds","description":"broken clouds"}],
                          "main":{"temp":280.15,"feels_like":278.50},
                          "wind":{"speed":3.60},
                          "visibility":10000,
                          "dt":1675744800,
                          "sys":{"sunrise":1675751262,"sunset":1675787560},
                          "timezone":10800,
                          "name":"Saint Petersburg"
                        }
                        """)));

        HttpClient client = HttpClient.newHttpClient();
        RequestExecutor http = HttpForwarding.forwarding(client, "http", "localhost", wireMock.port());

        WeatherApiClient weatherApiClient = new WeatherApiClient("TEST_KEY", http);
        GeocodingClient geocodingClient = new GeocodingClient("TEST_KEY", http); // не будет вызван
        CacheManager cache = new CacheManager(600_000L, 10);
        service = new WeatherService(weatherApiClient, geocodingClient, cache);
    }

    @AfterEach
    void tearDown() {
        wireMock.stop();
    }

    @Test
    void endToEndByCoordinates_fetchesAndCaches() {
        WeatherResponse byCoordinates = service.getByCoordinates(59.934280, 30.335099);
        assertThat(byCoordinates.getName()).isEqualTo("Saint Petersburg");
        verify(1, getRequestedFor(urlPathEqualTo("/data/2.5/weather")));

        service.getByCoordinates(59.934280, 30.335099);
        verify(1, getRequestedFor(urlPathEqualTo("/data/2.5/weather")));
    }
}
