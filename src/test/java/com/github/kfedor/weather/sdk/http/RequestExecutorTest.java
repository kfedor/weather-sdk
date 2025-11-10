package com.github.kfedor.weather.sdk.http;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RequestExecutorTest {

    private HttpClient httpClient;
    private RequestExecutor executor;

    @BeforeEach
    void setUp() {
        httpClient = mock(HttpClient.class);
        executor = new RequestExecutor(httpClient);
    }

    @Test
    void returnsResponseBodyWhenStatusCodeIs2xx() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn("{\"ok\":true}");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        URI uri = URI.create("https://api.example.com/weather");
        String body = executor.get(uri);

        assertThat(body).isEqualTo("{\"ok\":true}");
    }

    @Test
    void throwsExceptionWhenStatusCodeIsNot2xx() throws Exception {
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(response.body()).thenReturn("Not found");

        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(response);

        URI uri = URI.create("https://api.example.com/weather");

        assertThatThrownBy(() -> executor.get(uri))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("HTTP 404");
    }

    @Test
    void throwsExceptionWhenHttpClientFails() throws Exception {
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new WeatherSdkException("Network failure"));

        URI uri = URI.create("https://api.example.com/weather");

        assertThatThrownBy(() -> executor.get(uri))
                .isInstanceOf(WeatherSdkException.class)
                .hasMessageContaining("Network failure");
    }
}
