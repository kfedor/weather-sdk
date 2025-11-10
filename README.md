# Weather SDK (Java) — OpenWeather

A lightweight SDK library for retrieving current weather data from the OpenWeather API.
It supports TTL-based and LRU cache eviction, two operation modes (on-demand and polling),
a consistent JSON response structure, and a modular architecture suitable for production use.

---

## Features

- ✅ Fetch current weather by **city name** or **coordinates**
- ✅ **Cache**: TTL = 10 minutes, up to 10 entries, **LRU** eviction policy
- ✅ Two operating modes:
    - **ON_DEMAND** — API calls only when requested
    - **POLLING** — background cache refresh for zero-latency responses
- ✅ Unified `WeatherResponse` model (provider-independent)
- ✅ Throws meaningful exceptions (`WeatherSdkException`)
- ✅ Single instance per API key (registry control)

---

## Project Layout
Main packages:
- `core` — business logic, caching, service layer
- `http` — HTTP client, configuration, URL builder
- `model` — data transfer objects
- `mapper` — API-to-SDK mapping
- `registry` — instance registry
---

## API SDK (Public Interface)

* WeatherSdk sdk = WeatherSdkFactory.create(String apiKey, WeatherSdk.Mode mode);
* WeatherResponse getCurrentByCity(String city);
* WeatherResponse getCurrentByCoordinates(double latitude, double longitude);
* void destroy();
* enum Mode { ON_DEMAND, POLLING }

---

## Cache Behavior

- **TTL:** 10 minutes (data is considered valid within this period)
- **Capacity:** up to 10 entries
- **Eviction Policy:** LRU (Least Recently Used)
- **Cache keys:** normalized city names or `latitude=%.6f;longitude=%.6f`

---

## Error Handling

All runtime errors are wrapped in `WeatherSdkException`, including:
- network or HTTP failures
- "city not found" cases (empty geocoding responses)
- malformed JSON from provider

---

## System Requirements

- Java 21
- Maven 3.8+
- Lombok annotation processing enabled in IDE

---

## Testing

- **Unit tests:** JUnit 5, Mockito, AssertJ
- **Integration tests:** WireMock-based HTTP stubs
    - cover `byCity`, `byCoords`, background polling, and error paths

Run:
```
mvn test          # unit tests only
mvn verify        # full suite (unit + integration)
```

---

## Architecture Overview

- **Facade:** `WeatherSdk` — public entry point
- **Service layer:** `WeatherService` — business logic and caching
- **Clients:** `WeatherApiClient`, `GeocodingClient` — handle external API calls
- **Cache:** `CacheManager`, `CacheItem`, `RequestInfo`
- **Polling:** `PollingManager` — background refresh process
- **Mapping:** `WeatherMapper` — raw → SDK model conversion
- **HTTP layer:** `RequestExecutor`, `HttpClientProvider`, `UrlBuilder`, `HttpConfig`
- **Registry:** `KeyRegistry` — ensures one instance per API key

---

## License

MIT License  
Author: [@kfedor]

