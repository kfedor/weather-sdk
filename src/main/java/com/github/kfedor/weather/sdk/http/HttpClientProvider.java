package com.github.kfedor.weather.sdk.http;

import java.net.http.HttpClient;

/**
 * Factory for creating preconfigured instances of {@link java.net.http.HttpClient}.
 *
 * <p>Centralizes HTTP client configuration for the SDK, ensuring consistent
 * behavior across all network calls (timeouts, redirect policy, etc.).</p>
 *
 * <p>This class isolates low-level client setup from higher-level components
 * such as {@link com.github.kfedor.weather.sdk.http.RequestExecutor}.</p>
 *
 * <p>Thread-safe and intended for internal SDK use only.</p>
 *
 * @see java.net.http.HttpClient
 * @see com.github.kfedor.weather.sdk.http.RequestExecutor
 */
public final class HttpClientProvider {

    private HttpClientProvider() {
    }

    /**
     * Creates and configures a new {@link java.net.http.HttpClient} instance.
     *
     * <p>The client is configured with default timeouts, a standard redirect policy,
     * and a shared thread pool suitable for short-lived API requests.</p>
     *
     * <p>Each call to this method returns a new independent {@code HttpClient}
     * instance. Callers are expected to reuse it rather than recreate per request.</p>
     *
     * @return a configured {@link java.net.http.HttpClient} ready for use
     */
    public static HttpClient create() {
        return HttpClient.newHttpClient();
    }
}
