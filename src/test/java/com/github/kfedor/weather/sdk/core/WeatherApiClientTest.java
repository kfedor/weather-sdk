package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import com.github.kfedor.weather.sdk.model.OpenWeatherResponse;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WeatherApiClientTest {

    private RequestExecutor requestExecutor;
    private WeatherApiClient weatherApiClient;

    @BeforeEach
    void setUp() {
        requestExecutor = mock(RequestExecutor.class);
        weatherApiClient = new WeatherApiClient("TEST_KEY", requestExecutor);
    }

    @Test
    void byCoordinatesReturnsParsedResponse() throws Exception {
        String json = """
            {
              "weather":[{"main":"Clouds","description":"broken clouds"}],
              "main":{"temp":280.15,"feels_like":278.5},
              "wind":{"speed":3.6},
              "visibility":10000,
              "dt":1675744800,
              "sys":{"sunrise":1675751262,"sunset":1675787560},
              "timezone":3600,
              "name":"Helsinki"
            }
            """;

        when(requestExecutor.get(any(URI.class))).thenReturn(json);

        OpenWeatherResponse response = weatherApiClient.byCoordinates(60.1699, 24.9384);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Helsinki");
        assertThat(response.getMain()).isNotNull();
        assertThat(response.getMain().getTemp()).isCloseTo(280.15, within(1e-6));
        assertThat(response.getWeather()[0].getMain()).isEqualTo("Clouds");
        assertThat(response.getWind().getSpeed()).isCloseTo(3.6, within(1e-6));
    }

    @Test
    void byCoordinatesBuildsProperRequestUrl() throws Exception {
        when(requestExecutor.get(any(URI.class))).thenReturn("{}");

        weatherApiClient.byCoordinates(59.934280, 30.335099);

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(requestExecutor).get(uriCaptor.capture());

        String uri = uriCaptor.getValue().toString();
        assertThat(uri).contains("/data/2.5/weather");
        assertThat(uri).contains("appid=TEST_KEY");
        assertThat(uri).contains("lat=59.934280");
        assertThat(uri).contains("lon=30.335099");
    }

    @Test
    void byCoordinatesThrowsOnMalformedJson() throws Exception {
        when(requestExecutor.get(any(URI.class))).thenReturn("{invalid-json");

        assertThatThrownBy(() -> weatherApiClient.byCoordinates(10.0, 20.0))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("Malformed JSON");
    }

    private static org.assertj.core.data.Offset<Double> within(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
