package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

public final class WeatherPreferences {

    private static final String PREF_NAME = "WeatherAppPrefs";
    private static final String KEY_UNIT = "unit";
    private static final String KEY_LANGUAGE = "language";
    private static final String DEFAULT_UNIT = "metric";
    private static final String DEFAULT_LANGUAGE = "";

    private WeatherPreferences() {
    }

    public static String getUnit(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_UNIT, DEFAULT_UNIT);
    }

    public static void saveUnit(Context context, String unit) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_UNIT, unit).apply();
    }

    public static String getLanguage(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE);
    }

    public static void saveLanguage(Context context, String languageTag) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(KEY_LANGUAGE, languageTag).apply();
    }

    public static String getTemperatureUnitSymbol(Context context) {
        return "imperial".equals(getUnit(context)) ? "°F" : "°C";
    }

    public static String getWindSpeedSuffix(Context context) {
        return "imperial".equals(getUnit(context)) ? " mph" : " m/s";
    }
}
