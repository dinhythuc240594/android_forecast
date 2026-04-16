package com.example.myapplication.util;

import android.text.TextUtils;

public final class WeatherIconUrl {

    private static final String BASE = "https://openweathermap.org/img/wn/";

    private WeatherIconUrl() {
    }

    public static String forIcon(String iconCode) {
        if (TextUtils.isEmpty(iconCode)) {
            return null;
        }
        return BASE + iconCode + "@2x.png";
    }
}
