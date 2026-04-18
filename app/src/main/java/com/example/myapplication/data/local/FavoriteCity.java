package com.example.myapplication.data.local;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "favorite_city")
public class FavoriteCity {

    // dinh nghia ve tp yeu thich

    @PrimaryKey
    @NonNull
    public String cityName;

    public String weatherQuery;

    public long addedAt;

    public FavoriteCity(@NonNull String cityName, String weatherQuery, long addedAt) {
        this.cityName = cityName;
        this.weatherQuery = weatherQuery;
        this.addedAt = addedAt;
    }
}
