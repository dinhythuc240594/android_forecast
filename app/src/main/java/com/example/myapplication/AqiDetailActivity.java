package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class AqiDetailActivity extends AppCompatActivity {

    public static final String EXTRA_LAT = "extra_lat";
    public static final String EXTRA_LON = "extra_lon";
    public static final String EXTRA_CITY = "extra_city";
    private static final String REQUEST_TAG = "aqi_detail";

    private static final int[] AQI_COLORS = {
            0xFF4CAF50, // 1 - Good
            0xFF8BC34A, // 2 - Fair
            0xFFFFC107, // 3 - Moderate
            0xFFFF9800, // 4 - Poor
            0xFFF44336  // 5 - Very poor
    };

    private WeatherRepository weatherRepository;
    private TextView txtAqiCity;
    private TextView txtAqiValue;
    private TextView txtAqiLevel;
    private TextView txtAqiAdvice;
    private LinearProgressIndicator progressLoading;
    private View cardPollutants;

    private View rowPm25, rowPm10, rowO3, rowNo2, rowSo2, rowCo, rowNh3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aqi_detail);
        if (getSupportActionBar() != null) getSupportActionBar().hide();

        txtAqiCity = findViewById(R.id.txtAqiCity);
        txtAqiValue = findViewById(R.id.txtAqiValue);
        txtAqiLevel = findViewById(R.id.txtAqiLevel);
        txtAqiAdvice = findViewById(R.id.txtAqiAdvice);
        progressLoading = findViewById(R.id.progressAqiLoading);
        cardPollutants = findViewById(R.id.cardPollutants);

        rowPm25 = findViewById(R.id.rowPm25);
        rowPm10 = findViewById(R.id.rowPm10);
        rowO3 = findViewById(R.id.rowO3);
        rowNo2 = findViewById(R.id.rowNo2);
        rowSo2 = findViewById(R.id.rowSo2);
        rowCo = findViewById(R.id.rowCo);
        rowNh3 = findViewById(R.id.rowNh3);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        weatherRepository = new WeatherRepository(this);

        double lat = getIntent().getDoubleExtra(EXTRA_LAT, 0);
        double lon = getIntent().getDoubleExtra(EXTRA_LON, 0);
        String city = getIntent().getStringExtra(EXTRA_CITY);

        txtAqiCity.setText(city != null ? city : "");
        cardPollutants.setVisibility(View.GONE);

        if (lat != 0 || lon != 0) {
            fetchData(lat, lon);
        } else {
            Toast.makeText(this, R.string.aqi_error_no_location, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherRepository.cancel(REQUEST_TAG);
    }

    private void fetchData(double lat, double lon) {
        progressLoading.setVisibility(View.VISIBLE);
        weatherRepository.fetchAirQualityDetail(lat, lon, REQUEST_TAG, new WeatherRepository.AirQualityCallback() {
            @Override
            public void onSuccess(AirQualityData data) {
                progressLoading.setVisibility(View.GONE);
                displayData(data);
            }

            @Override
            public void onError(String message) {
                progressLoading.setVisibility(View.GONE);
                Toast.makeText(AqiDetailActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayData(AirQualityData data) {
        int aqi = data.aqi;
        txtAqiValue.setText(String.valueOf(aqi));

        String[] levels = getResources().getStringArray(R.array.aqi_levels);
        String[] advices = getResources().getStringArray(R.array.aqi_advices);

        int idx = Math.max(0, Math.min(aqi - 1, levels.length - 1));
        txtAqiLevel.setText(levels[idx]);
        txtAqiLevel.setTextColor(AQI_COLORS[idx]);
        txtAqiAdvice.setText(advices[idx]);

        cardPollutants.setVisibility(View.VISIBLE);

        bindRow(rowPm25, "PM2.5", data.pm2_5, 75.0, getColorForPollutant(data.pm2_5, 10, 25, 50, 75));
        bindRow(rowPm10, "PM10", data.pm10, 150.0, getColorForPollutant(data.pm10, 20, 50, 100, 200));
        bindRow(rowO3, "O₃", data.o3, 240.0, getColorForPollutant(data.o3, 60, 100, 140, 180));
        bindRow(rowNo2, "NO₂", data.no2, 200.0, getColorForPollutant(data.no2, 40, 70, 150, 200));
        bindRow(rowSo2, "SO₂", data.so2, 350.0, getColorForPollutant(data.so2, 20, 80, 250, 350));
        bindRow(rowCo, "CO", data.co, 15400.0, getColorForPollutant(data.co, 4400, 9400, 12400, 15400));
        bindRow(rowNh3, "NH₃", data.nh3, 200.0, getColorForPollutant(data.nh3, 25, 50, 100, 200));
    }

    private void bindRow(View row, String name, double value, double maxRef, int color) {
        TextView txtName = row.findViewById(R.id.txtPollutantName);
        TextView txtValue = row.findViewById(R.id.txtPollutantValue);
        ProgressBar progress = row.findViewById(R.id.progressPollutant);

        txtName.setText(name);
        txtValue.setText(String.format(Locale.getDefault(), "%.1f µg/m³", value));
        int pct = (int) Math.min(100, (value / maxRef) * 100);
        progress.setProgress(pct);
        progress.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.SRC_IN);
    }

    private int getColorForPollutant(double value, double good, double fair, double moderate, double poor) {
        if (value <= good) return AQI_COLORS[0];
        if (value <= fair) return AQI_COLORS[1];
        if (value <= moderate) return AQI_COLORS[2];
        if (value <= poor) return AQI_COLORS[3];
        return AQI_COLORS[4];
    }

    public static Intent newIntent(Context context, double lat, double lon, String cityName) {
        Intent intent = new Intent(context, AqiDetailActivity.class);
        intent.putExtra(EXTRA_LAT, lat);
        intent.putExtra(EXTRA_LON, lon);
        intent.putExtra(EXTRA_CITY, cityName);
        return intent;
    }
}
