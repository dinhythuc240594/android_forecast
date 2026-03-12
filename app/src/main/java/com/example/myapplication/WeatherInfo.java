package com.example.myapplication;

public class WeatherInfo {

    private final String cityName;
    private final double temperature;
    private final String description;
    private final int humidity;
    private final double windSpeed;

    public WeatherInfo(String cityName, double temperature, String description, int humidity, double windSpeed) {
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
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
}
