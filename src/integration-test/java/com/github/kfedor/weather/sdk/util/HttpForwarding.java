package com.github.kfedor.weather.sdk.util;

import com.github.kfedor.weather.sdk.http.RequestExecutor;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class HttpForwarding {

    private HttpForwarding() {
    }

    public static RequestExecutor forwarding(HttpClient client, String scheme, String host, int port) {
        return new RequestExecutor(client) {
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
                throw new com.github.kfedor.weather.sdk.exception.WeatherSdkException("HTTP error " + code + " from provider");
            }

            private URI rewrite(URI original) {
                String path = original.getPath();
                String query = original.getQuery();
                String newUri = scheme + "://" + host + ":" + port + path + (query == null ? "" : "?" + query);
                return URI.create(newUri);
            }
        };
    }
}
