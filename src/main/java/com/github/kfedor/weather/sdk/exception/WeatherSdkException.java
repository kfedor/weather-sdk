package com.github.kfedor.weather.sdk.exception;

public class WeatherSdkException extends RuntimeException {
    public WeatherSdkException(String message) { super(message); }
    public WeatherSdkException(String message, Throwable cause) { super(message, cause); }
}