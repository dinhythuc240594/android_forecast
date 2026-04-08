package com.example.myapplication;

public class WeatherInfo {

    private final String cityName;
    private final double temperature;
    private final String description;
    private final int humidity;
    private final double windSpeed;
    private int aqi;
    private final double uv;
    private final double latitude;
    private final double longitude;
    private final boolean hasCoordinates;

    public WeatherInfo(
            String cityName,
            double temperature,
            String description,
            int humidity,
            double windSpeed,
            int aqi,
            double uv,
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
        this.aqi = aqi;
        this.uv = uv;
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

    public int getAqi(){
        return aqi;
    }

    public void setAqi(int aqi){
        this.aqi = aqi;
    }

    public double getUv(){
        return  uv;
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
