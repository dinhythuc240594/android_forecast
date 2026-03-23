package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String WEATHER_REQUEST_TAG = "detail_weather";

    private TextView txtDetailCity, txtDetailTemp, txtDetailDesc, txtHumidity, txtWind;
    private LinearProgressIndicator progressDetailLoading;
    private WeatherRepository weatherRepository;
    private String cityName;
    private String currentUnit;
    private String currentLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ View
        txtDetailCity = findViewById(R.id.txtDetailCity);
        txtDetailTemp = findViewById(R.id.txtDetailTemp);
        txtDetailDesc = findViewById(R.id.txtDetailDesc);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtWind = findViewById(R.id.txtWind);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        progressDetailLoading = findViewById(R.id.progressDetailLoading);
        weatherRepository = new WeatherRepository(this);
        currentUnit = WeatherPreferences.getUnit(this);
        currentLanguage = LanguageHelper.getCurrentLanguage(this);

        // Nút quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu (Tên thành phố) từ Intent
        Intent intent = getIntent();
        cityName = intent.getStringExtra("CITY_NAME");

        if (cityName != null && !cityName.isEmpty()) {
            txtDetailCity.setText(cityName);
            if (WeatherRepository.hasApiKey()) {
                fetchWeatherDetails(cityName);
            } else {
                showWeatherError(getString(R.string.detail_missing_api_key));
            }
        } else {
            Toast.makeText(this, R.string.detail_missing_city, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedUnit = WeatherPreferences.getUnit(this);
        String selectedLanguage = LanguageHelper.getCurrentLanguage(this);
        if (cityName != null && (!selectedUnit.equals(currentUnit) || !selectedLanguage.equals(currentLanguage))) {
            currentUnit = selectedUnit;
            currentLanguage = selectedLanguage;
            fetchWeatherDetails(cityName);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherRepository.cancel(WEATHER_REQUEST_TAG);
        progressDetailLoading.setVisibility(View.GONE);
    }

    private void fetchWeatherDetails(String cityName) {
        currentUnit = WeatherPreferences.getUnit(this);
        txtDetailDesc.setText(R.string.detail_loading);
        progressDetailLoading.setVisibility(View.VISIBLE);
        weatherRepository.fetchWeatherByCity(cityName, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                progressDetailLoading.setVisibility(View.GONE);
                txtDetailCity.setText(weatherInfo.getCityName());
                txtDetailTemp.setText(Math.round(weatherInfo.getTemperature()) + WeatherPreferences.getTemperatureUnitSymbol(DetailActivity.this));
                txtDetailDesc.setText(weatherInfo.getDescription());
                txtHumidity.setText(weatherInfo.getHumidity() + "%");
                txtWind.setText(String.format(
                        Locale.getDefault(),
                        "%.1f%s",
                        weatherInfo.getWindSpeed(),
                        WeatherPreferences.getWindSpeedSuffix(DetailActivity.this)
                ));
            }

            @Override
            public void onError(String message) {
                progressDetailLoading.setVisibility(View.GONE);
                showWeatherError(message);
            }
        });
    }

    private void showWeatherError(String message) {
        progressDetailLoading.setVisibility(View.GONE);
        txtDetailTemp.setText("--");
        txtDetailDesc.setText(message);
        txtHumidity.setText("--%");
        txtWind.setText("--");
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}