package com.example.myapplication.data.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public final class DailyForecast implements Serializable {

    // dinh nghia lop du bao bao hang ngay
    private final String dayLabel;
    private final int popPercent;
    private final String iconDay;
    private final String iconNight;
    private final int tempMax;
    private final int tempMin;
    private final List<DaySlot> slots;

    public DailyForecast(
            String dayLabel,
            int popPercent,
            String iconDay,
            String iconNight,
            int tempMax,
            int tempMin,
            List<DaySlot> slots
    ) {
        this.dayLabel = dayLabel;
        this.popPercent = popPercent;
        this.iconDay = iconDay;
        this.iconNight = iconNight;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.slots = slots != null ? slots : Collections.emptyList();
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

    public List<DaySlot> getSlots() {
        return slots;
    }
}
