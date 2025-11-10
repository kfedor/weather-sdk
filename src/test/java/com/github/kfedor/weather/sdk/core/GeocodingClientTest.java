package com.github.kfedor.weather.sdk.core;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import com.github.kfedor.weather.sdk.http.RequestExecutor;
import java.net.URI;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GeocodingClientTest {

    private RequestExecutor requestExecutor;
    private GeocodingClient geocodingClient;

    @BeforeEach
    void setUp() {
        requestExecutor = mock(RequestExecutor.class);
        geocodingClient = new GeocodingClient("TEST_KEY", requestExecutor);
    }

    @Test
    void resolveFirstReturnsLocationWhenFound() throws Exception {
        String json = "[{\"lat\":60.1699,\"lon\":24.9384}]";
        when(requestExecutor.get(any(URI.class))).thenReturn(json);

        Optional<GeocodingClient.Location> result = geocodingClient.findFirstLocation("Helsinki");

        assertThat(result).isPresent();
        assertThat(result.get().latitude()).isCloseTo(60.1699, within(1e-6));
        assertThat(result.get().longitude()).isCloseTo(24.9384, within(1e-6));
    }

    @Test
    void resolveFirstReturnsEmptyWhenNoResults() throws Exception {
        when(requestExecutor.get(any(URI.class))).thenReturn("[]");

        Optional<GeocodingClient.Location> result = geocodingClient.findFirstLocation("Nowhere City");

        assertThat(result).isEmpty();
    }

    @Test
    void resolveFirstThrowsOnMalformedJson() throws Exception {
        when(requestExecutor.get(any(URI.class))).thenReturn("{not-valid-json");

        assertThatThrownBy(() -> geocodingClient.findFirstLocation("Paris"))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("Malformed JSON");
    }

    @Test
    void resolveFirstBuildsUrlWithQueryParameters() throws Exception {
        when(requestExecutor.get(any(URI.class))).thenReturn("[]");

        geocodingClient.findFirstLocation("Saint Petersburg");

        ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
        verify(requestExecutor).get(uriCaptor.capture());

        String uri = uriCaptor.getValue().toString();
        assertThat(uri).contains("/geo/1.0/direct");
        assertThat(uri).contains("appid=TEST_KEY");
        assertThat(uri).contains("limit=1");

        assertThat(uri).matches(".*q=Saint\\+Petersburg.*");
    }

    private static org.assertj.core.data.Offset<Double> within(double delta) {
        return org.assertj.core.data.Offset.offset(delta);
    }
}
