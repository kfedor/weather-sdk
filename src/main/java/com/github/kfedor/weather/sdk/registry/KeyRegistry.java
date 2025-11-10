package com.github.kfedor.weather.sdk.registry;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * KeyRegistry keeps track of active SDK instances associated with API keys.
 * <p>
 * Ensures that only one SDK instance exists per unique API key.
 * <p>
 * Internally uses {@link java.lang.ref.WeakReference} so that entries
 * are automatically cleared when the corresponding SDK instances
 * are garbage-collected or explicitly destroyed.
 *
 * @param <T> the type of object being registered (e.g., WeatherSdk)
 */
public final class KeyRegistry<T> {

    private final Map<String, WeakReference<T>> map = new ConcurrentHashMap<>();

    /**
     * Retrieves an active object associated with the given key.
     *
     * <p>If the object has been garbage-collected or unregistered,
     * this method returns {@code null} and removes the stale reference
     * from the registry.</p>
     *
     * @param key unique identifier (typically an API key)
     * @return the registered object, or {@code null} if none exists
     */
    public T get(String key) {
        WeakReference<T> reference = map.get(key);
        return reference == null ? null : reference.get();
    }

    /**
     * Registers a new object under the specified key.
     *
     * <p>The object is stored as a {@link java.lang.ref.WeakReference}
     * so that it can be automatically removed when garbage-collected.</p>
     *
     * <p>If an entry with the same key already exists, it will be replaced.</p>
     *
     * @param key   unique identifier (e.g. API key)
     * @param value object to associate with the key
     */
    public void put(String key, T value) {
        map.put(key, new WeakReference<>(value));
    }

    /**
     * Removes a registry entry if its stored reference matches the given object.
     *
     * <p>Used to safely unregister SDK instances when they are explicitly
     * destroyed, without needing to know the associated key.</p>
     *
     * <p>This method iterates through all entries and removes any whose
     * {@link java.lang.ref.WeakReference} points to the specified object.</p>
     *
     * @param value the object instance to remove from the registry
     */
    public void removeIfMatches(T value) {
        map.entrySet().removeIf(referenceEntry -> referenceEntry.getValue().get() == value);
    }
}
