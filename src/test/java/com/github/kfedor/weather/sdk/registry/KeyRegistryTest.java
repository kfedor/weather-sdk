package com.github.kfedor.weather.sdk.registry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KeyRegistryTest {

    private KeyRegistry<Dummy> registry;

    private static class Dummy {
        final String name;

        Dummy(String name) {
            this.name = name;
        }
    }

    @BeforeEach
    void setUp() {
        registry = new KeyRegistry<>();
    }

    @Test
    void storesAndRetrievesValue() {
        Dummy dummy = new Dummy("alpha");
        registry.put("key1", dummy);

        Dummy retrieved = registry.get("key1");

        assertThat(retrieved).isSameAs(dummy);
    }

    @Test
    void returnsNullWhenReferenceClearedManually() {
        String key = "gc-test";
        Dummy dummy = new Dummy("temp");
        registry.put(key, dummy);

        registry.removeIfMatches(dummy);

        Dummy retrieved = registry.get(key);
        assertThat(retrieved).isNull();
    }

    @Test
    void removeIfMatchesRemovesMatchingReference() {
        Dummy dummy1 = new Dummy("first");
        Dummy dummy2 = new Dummy("second");

        registry.put("key1", dummy1);
        registry.put("key2", dummy2);

        registry.removeIfMatches(dummy1);

        assertThat(registry.get("key1")).isNull();
        assertThat(registry.get("key2")).isSameAs(dummy2);
    }

    @Test
    void removeIfMatchesDoesNothingIfReferenceNotFound() {
        Dummy dummy = new Dummy("main");
        registry.put("keyA", dummy);

        Dummy nonExisting = new Dummy("ghost");
        registry.removeIfMatches(nonExisting);

        assertThat(registry.get("keyA")).isSameAs(dummy);
    }
}