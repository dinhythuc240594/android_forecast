package com.example.myapplication;

public final class HourlyForecast {

    private final String timeLabel;
    private final double temp;
    private final int popPercent;
    private final String iconCode;

    public HourlyForecast(String timeLabel, double temp, int popPercent, String iconCode) {
        this.timeLabel = timeLabel;
        this.temp = temp;
        this.popPercent = popPercent;
        this.iconCode = iconCode;
    }

    public String getTimeLabel() {
        return timeLabel;
    }

    public double getTemp() {
        return temp;
    }

    public int getPopPercent() {
        return popPercent;
    }

    public String getIconCode() {
        return iconCode;
    }
}
