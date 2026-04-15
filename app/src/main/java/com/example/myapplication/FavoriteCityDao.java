package com.example.myapplication;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteCityDao {

    @Query("SELECT * FROM favorite_city ORDER BY addedAt DESC")
    List<FavoriteCity> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(FavoriteCity city);

    @Delete
    void delete(FavoriteCity city);

    @Query("SELECT COUNT(*) FROM favorite_city WHERE cityName = :name")
    int countByName(String name);

    @Query("DELETE FROM favorite_city WHERE cityName = :name")
    void deleteByName(String name);
}
