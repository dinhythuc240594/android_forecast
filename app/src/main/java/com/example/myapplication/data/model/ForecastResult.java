package com.example.myapplication.data.model;

import java.util.Collections;
import java.util.List;

public final class ForecastResult {

    // lop dinh nghia mang du bao theo ngay va gio
    private final List<HourlyForecast> hourly;
    private final List<DailyForecast> daily;

    public ForecastResult(List<HourlyForecast> hourly, List<DailyForecast> daily) {
        this.hourly = hourly != null ? hourly : Collections.emptyList();
        this.daily = daily != null ? daily : Collections.emptyList();
    }

    public List<HourlyForecast> getHourly() {
        return hourly;
    }

    public List<DailyForecast> getDaily() {
        return daily;
    }

    public int hourlyMinTempRounded() {
        if (hourly.isEmpty()) {
            return 0;
        }
        int min = (int) Math.round(hourly.get(0).getTemp());
        for (HourlyForecast h : hourly) {
            min = Math.min(min, (int) Math.round(h.getTemp()));
        }
        return min;
    }
}
