package com.github.kfedor.weather.sdk.http;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Utility class for building complete API request URLs.
 *
 * <p>Provides helper methods to safely append query parameters
 * to a base URL and ensure proper URL encoding for all values.</p>
 *
 * <p>This class is stateless and thread-safe.</p>
 *
 * @see java.net.URI
 */
public final class UrlBuilder {

    private UrlBuilder() {
    }

    /**
     * Builds a complete URL by appending query parameters to a base URL.
     *
     * <p>If the provided parameter map is {@code null} or empty,
     * the base URL is returned as-is. All parameter names and values
     * are URL-encoded to ensure safe transmission over HTTP.</p>
     *
     * <p>Parameters are added in sorted order to guarantee deterministic
     * output (useful for testing and caching).</p>
     *
     * @param base   the base endpoint URL (without query string)
     * @param params key–value pairs of query parameters
     * @return a fully constructed {@link java.net.URI} representing the request URL
     */
    public static URI build(String base, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return URI.create(base);
        }
        StringBuilder stringBuilder = new StringBuilder(base).append("?");
        boolean first = true;
        for (String key : params.keySet().stream().sorted().toList()) {
            String value = params.get(key);
            if (value == null || value.isBlank()) {
                continue;
            }
            if (!first) {
                stringBuilder.append("&");
            }
            first = false;
            stringBuilder.append(enc(key)).append("=").append(enc(value));
        }
        return URI.create(stringBuilder.toString());
    }

    private static String enc(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }
}
