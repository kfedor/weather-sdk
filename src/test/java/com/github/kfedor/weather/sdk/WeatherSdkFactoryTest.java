package com.github.kfedor.weather.sdk;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WeatherSdkFactoryTest {

    @Test
    void returnsSameInstanceForSameApiKey() {
        WeatherSdk a = WeatherSdkFactory.create("KEY_A", WeatherSdk.Mode.ON_DEMAND);
        WeatherSdk b = WeatherSdkFactory.create("KEY_A", WeatherSdk.Mode.ON_DEMAND);

        assertThat(a).isSameAs(b);
    }

    @Test
    void returnsDifferentInstancesForDifferentApiKeys() {
        WeatherSdk a = WeatherSdkFactory.create("KEY_1", WeatherSdk.Mode.ON_DEMAND);
        WeatherSdk b = WeatherSdkFactory.create("KEY_2", WeatherSdk.Mode.ON_DEMAND);

        assertThat(a).isNotSameAs(b);
    }

    @Test
    void createsNewInstanceAfterDestroyWithSameApiKey() {
        WeatherSdk initial = WeatherSdkFactory.create("KEY_REUSABLE", WeatherSdk.Mode.ON_DEMAND);
        initial.destroy();

        WeatherSdk recreated = WeatherSdkFactory.create("KEY_REUSABLE", WeatherSdk.Mode.ON_DEMAND);

        assertThat(recreated).isNotSameAs(initial);
    }
}
