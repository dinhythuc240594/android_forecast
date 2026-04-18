package com.example.myapplication.data.model;

import java.io.Serializable;

public final class DaySlot implements Serializable {

    // lop dinh nghia bieu do theo ngay cua du bao thoi tiet
    private final String timeLabel;
    private final double temp;
    private final int popPercent;
    private final String iconCode;
    private final int humidity;
    private final double windSpeed;

    public DaySlot(String timeLabel, double temp, int popPercent, String iconCode,
                   int humidity, double windSpeed) {
        this.timeLabel = timeLabel;
        this.temp = temp;
        this.popPercent = popPercent;
        this.iconCode = iconCode;
        this.humidity = humidity;
        this.windSpeed = windSpeed;
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

    public int getHumidity() {
        return humidity;
    }

    public double getWindSpeed() {
        return windSpeed;
    }
}
