package com.github.kfedor.weather.sdk.http;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UrlBuilderTest {

    @Test
    void buildsUriWithSortedAndEncodedParameters() {
        String base = "https://api.example.com/weather";

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("longitude", "30.335099");
        parameters.put("q", "Saint Petersburg");
        parameters.put("appid", "KEY-123!");
        parameters.put("latitude", "59.934280");

        URI uri = UrlBuilder.build(base, parameters);

        String expected = "https://api.example.com/weather"
                          + "?appid=KEY-123%21"
                          + "&latitude=59.934280"
                          + "&longitude=30.335099"
                          + "&q=Saint+Petersburg";

        assertThat(uri.toString()).isEqualTo(expected);
    }

    @Test
    void returnsBaseUriWhenParametersAreEmpty() {
        String base = "https://api.example.com/geo";
        Map<String, String> parameters = Map.of();

        URI uri = UrlBuilder.build(base, parameters);

        assertThat(uri.toString()).isEqualTo(base);
    }

    @Test
    void returnsBaseUriWhenParametersAreNull() {
        String base = "https://api.example.com/geo";

        URI uri = UrlBuilder.build(base, null);

        assertThat(uri.toString()).isEqualTo(base);
    }

    @Test
    void skipsNullOrBlankParameters() {
        String base = "https://api.example.com/endpoint";

        Map<String, String> parameters = new LinkedHashMap<>();
        parameters.put("appid", "KEY");
        parameters.put("latitude", "");
        parameters.put("longitude", null);

        URI uri = UrlBuilder.build(base, parameters);

        String expected = "https://api.example.com/endpoint?appid=KEY";
        assertThat(uri.toString()).isEqualTo(expected);
    }
}
