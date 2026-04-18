package com.example.myapplication.data.model;

public final class HourlyForecast {

    // lop dinh nghia bieu do theo gio cua du bao thoi tiet

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
