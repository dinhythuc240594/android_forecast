package com.example.myapplication.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.myapplication.data.model.WeatherInfo;

@Entity(tableName = "cached_weather")
public class CachedWeather {

    // luu cache ve thong tin thoi tiet tp

    @PrimaryKey
    @NonNull
    public String cacheKey;

    public String cityName;
    public double temperature;
    public String description;
    public int humidity;
    public double windSpeed;
    public int aqi;
    public double uv;
    public double latitude;
    public double longitude;
    public String unit;
    public long updatedAt;
    public int weatherId;

    public CachedWeather(@NonNull String cacheKey, String cityName, double temperature,
                         String description, int humidity, double windSpeed, int aqi,
                         double uv, double latitude, double longitude, String unit,
                         long updatedAt, int weatherId) {
        this.cacheKey = cacheKey;
        this.cityName = cityName;
        this.temperature = temperature;
        this.description = description;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
        this.aqi = aqi;
        this.uv = uv;
        this.latitude = latitude;
        this.longitude = longitude;
        this.unit = unit;
        this.updatedAt = updatedAt;
        this.weatherId = weatherId;
    }

    public WeatherInfo toWeatherInfo() {
        return new WeatherInfo(cityName, temperature, description, humidity,
                windSpeed, aqi, uv, latitude, longitude, true, weatherId);
    }

    public static CachedWeather fromWeatherInfo(String cacheKey, WeatherInfo info, String unit) {
        return new CachedWeather(
                cacheKey,
                info.getCityName(),
                info.getTemperature(),
                info.getDescription(),
                info.getHumidity(),
                info.getWindSpeed(),
                info.getAqi(),
                info.getUv(),
                info.getLatitude(),
                info.getLongitude(),
                unit,
                System.currentTimeMillis(),
                info.getWeatherId()
        );
    }
}
