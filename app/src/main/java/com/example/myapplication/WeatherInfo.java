package com.example.myapplication;

public class WeatherInfo {

    private final String cityName;
    private final double temperature;
    private final String description;
    private final int humidity;
    private final double windSpeed;
    private final double latitude;
    private final double longitude;
    private final boolean hasCoordinates;

    public WeatherInfo(
            String cityName,
            double temperature,
            String description,
            int humidity,
            double windSpeed,
            double latitude,
            double longitude,
            boolean hasCoordinates
    ) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.latitude = latitude;
        this.longitude = longitude;
        this.hasCoordinates = hasCoordinates;
    }

    public String getCityName() {
        return cityName;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getDescription() {
        return description;
    }

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public boolean hasCoordinates() {
        return hasCoordinates;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
