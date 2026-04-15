package com.example.myapplication;

import java.io.Serializable;

public class AirQualityData implements Serializable {

    public final int aqi;
    public final double co;
    public final double no;
    public final double no2;
    public final double o3;
    public final double so2;
    public final double pm2_5;
    public final double pm10;
    public final double nh3;

    public AirQualityData(int aqi, double co, double no, double no2, double o3,
                          double so2, double pm2_5, double pm10, double nh3) {
        this.aqi = aqi;
        this.co = co;
        this.no = no;
        this.no2 = no2;
        this.o3 = o3;
        this.so2 = so2;
        this.pm2_5 = pm2_5;
        this.pm10 = pm10;
        this.nh3 = nh3;
    }
}
