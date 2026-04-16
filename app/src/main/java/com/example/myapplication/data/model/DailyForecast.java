package com.example.myapplication.data.model;

public final class DailyForecast {

    private final String dayLabel;
    private final int popPercent;
    private final String iconDay;
    private final String iconNight;
    private final int tempMax;
    private final int tempMin;

    public DailyForecast(
            String dayLabel,
            int popPercent,
            String iconDay,
            String iconNight,
            int tempMax,
            int tempMin
    ) {
        this.dayLabel = dayLabel;
        this.popPercent = popPercent;
        this.iconDay = iconDay;
        this.iconNight = iconNight;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
    }

    public String getDayLabel() {
        return dayLabel;
    }

    public int getPopPercent() {
        return popPercent;
    }

    public String getIconDay() {
        return iconDay;
    }

    public String getIconNight() {
        return iconNight;
    }

    public int getTempMax() {
        return tempMax;
    }

    public int getTempMin() {
        return tempMin;
    }
}
