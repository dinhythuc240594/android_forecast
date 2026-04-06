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
    /** Tên hiển thị (tiếng Việt từ API địa phương hoặc tên người dùng chọn). */
    private String displayCityName;
    /** Tham số {@code q} gửi OpenWeather; có thể dạng "Hà Nội,VN". */
    private String weatherQuery;
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

        Intent intent = getIntent();
        displayCityName = intent.getStringExtra("CITY_NAME");
        weatherQuery = intent.getStringExtra("WEATHER_QUERY");
        if (weatherQuery == null || weatherQuery.isEmpty()) {
            weatherQuery = displayCityName;
        }

        if (displayCityName != null && !displayCityName.isEmpty()) {
            txtDetailCity.setText(displayCityName);
            if (WeatherRepository.hasApiKey()) {
                fetchWeatherDetails();
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
        if (displayCityName != null && (!selectedUnit.equals(currentUnit) || !selectedLanguage.equals(currentLanguage))) {
            currentUnit = selectedUnit;
            currentLanguage = selectedLanguage;
            fetchWeatherDetails();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherRepository.cancel(WEATHER_REQUEST_TAG);
        progressDetailLoading.setVisibility(View.GONE);
    }

    private void fetchWeatherDetails() {
        currentUnit = WeatherPreferences.getUnit(this);
        txtDetailDesc.setText(R.string.detail_loading);
        progressDetailLoading.setVisibility(View.VISIBLE);
        weatherRepository.fetchWeatherByCity(weatherQuery, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                progressDetailLoading.setVisibility(View.GONE);
                txtDetailCity.setText(displayCityName != null ? displayCityName : weatherInfo.getCityName());
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