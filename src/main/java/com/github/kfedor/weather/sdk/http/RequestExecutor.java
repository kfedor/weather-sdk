package com.github.kfedor.weather.sdk.http;

import com.github.kfedor.weather.sdk.exception.WeatherSdkException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Executes low-level HTTP requests for the Weather SDK.
 *
 * <p>Wraps Java’s built-in {@link java.net.http.HttpClient} to perform
 * HTTP operations and handle common error scenarios such as
 * network timeouts or invalid responses.</p>
 *
 * <p>This class provides minimal abstraction over the raw HTTP layer,
 * leaving higher-level concerns (like retries, caching, or parsing)
 * to other SDK components.</p>
 *
 * <p>Designed for internal use within the SDK’s {@code http} package.</p>
 *
 * @see java.net.http.HttpClient
 */
public class RequestExecutor {

    private final HttpClient client;

    public RequestExecutor(HttpClient client) {
        this.client = client;
    }

    /**
     * Performs an HTTP GET request to the specified URI and returns the response body.
     *
     * <p>The method executes a blocking request using the configured
     * {@link java.net.http.HttpClient}. If the response status code is not 2xx,
     * a {@link com.github.kfedor.weather.sdk.exception.WeatherSdkException} is thrown.</p>
     *
     * @param uri the target URI for the GET request
     * @return the raw response body as a string
     * @throws com.github.kfedor.weather.sdk.exception.WeatherSdkException if the request fails or a non-successful status code is returned
     */
    public String get(URI uri) throws IOException, InterruptedException {
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .header("Accept", "application/json")
                .GET()
                .build();
        HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        int code = httpResponse.statusCode();

        if (code >= 200 && code < 300) {
            return httpResponse.body();
        }
        if (code == 401 || code == 403) {
            throw new WeatherSdkException("Unauthorized: bad API key (HTTP " + code + ")");
        }
        if (code == 404) {
            throw new WeatherSdkException("Not found (HTTP 404)");
        }
        throw new WeatherSdkException("HTTP error " + code + " from provider");
    }
}
