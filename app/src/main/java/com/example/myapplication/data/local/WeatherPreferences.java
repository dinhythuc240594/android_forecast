package com.example.myapplication.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public final class WeatherPreferences {

    // dinh nghia ve cac gia tri man hinh cai dat

    private static final String PREF_NAME = "WeatherAppPrefs";
    private static final String KEY_UNIT = "unit";
    private static final String KEY_LANGUAGE = "language";
    private static final String KEY_NOTIF_ENABLED = "daily_notif_enabled";
    private static final String KEY_NOTIF_HOUR = "daily_notif_hour";
    private static final String KEY_NOTIF_MINUTE = "daily_notif_minute";
    private static final String DEFAULT_UNIT = "metric";
    private static final String DEFAULT_LANGUAGE = "";
    private static final int DEFAULT_NOTIF_HOUR = 7;
    private static final int DEFAULT_NOTIF_MINUTE = 0;

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

    public static boolean isDailyNotifEnabled(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_NOTIF_ENABLED, false);
    }

    public static void setDailyNotifEnabled(Context context, boolean enabled) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit().putBoolean(KEY_NOTIF_ENABLED, enabled).apply();
    }

    public static int getDailyNotifHour(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_NOTIF_HOUR, DEFAULT_NOTIF_HOUR);
    }

    public static int getDailyNotifMinute(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_NOTIF_MINUTE, DEFAULT_NOTIF_MINUTE);
    }

    public static void setDailyNotifTime(Context context, int hour, int minute) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putInt(KEY_NOTIF_HOUR, hour)
                .putInt(KEY_NOTIF_MINUTE, minute)
                .apply();
    }
}
