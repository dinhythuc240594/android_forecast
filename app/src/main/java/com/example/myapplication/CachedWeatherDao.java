package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface CachedWeatherDao {

    @Query("SELECT * FROM cached_weather WHERE cacheKey = :key AND unit = :unit LIMIT 1")
    CachedWeather get(String key, String unit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(CachedWeather cached);

    @Query("DELETE FROM cached_weather WHERE updatedAt < :cutoffMs")
    void deleteOlderThan(long cutoffMs);
}
