package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    private static final String WEATHER_REQUEST_TAG = "detail_weather";
    private static final String FORECAST_REQUEST_TAG = "detail_forecast";

    private static final String[] AQI_LEVELS = {"Tốt", "Khá", "Trung bình", "Kém", "Rất kém"};

    private TextView txtDetailCity, txtDetailTemp, txtDetailDesc, txtHumidity, txtWind, txtAQI, txtUV;
    private TextView txtHourlySummary;
    private LinearProgressIndicator progressDetailLoading;
    private View layoutForecastSections;
    private RecyclerView rvHourly;
    private RecyclerView rvDaily;
    private final List<HourlyForecast> hourlyForecastItems = new ArrayList<>();
    private final List<DailyForecast> dailyForecastItems = new ArrayList<>();
    private HourlyForecastAdapter hourlyForecastAdapter;
    private DailyForecastAdapter dailyForecastAdapter;
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

        txtDetailCity = findViewById(R.id.txtDetailCity);
        txtDetailTemp = findViewById(R.id.txtDetailTemp);
        txtDetailDesc = findViewById(R.id.txtDetailDesc);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtWind = findViewById(R.id.txtWind);
        txtAQI = findViewById(R.id.txtAQI);
        txtUV = findViewById(R.id.txtUV);
        txtHourlySummary = findViewById(R.id.txtHourlySummary);
        layoutForecastSections = findViewById(R.id.layoutForecastSections);
        rvHourly = findViewById(R.id.rvHourly);
        rvDaily = findViewById(R.id.rvDaily);
        MaterialButton btnBack = findViewById(R.id.btnBack);
        progressDetailLoading = findViewById(R.id.progressDetailLoading);
        weatherRepository = new WeatherRepository(this);
        currentUnit = WeatherPreferences.getUnit(this);
        currentLanguage = LanguageHelper.getCurrentLanguage(this);

        hourlyForecastAdapter = new HourlyForecastAdapter(hourlyForecastItems);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyForecastAdapter);
        rvHourly.setHasFixedSize(true);

        dailyForecastAdapter = new DailyForecastAdapter(dailyForecastItems);
        rvDaily.setLayoutManager(new LinearLayoutManager(this));
        rvDaily.setAdapter(dailyForecastAdapter);

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
        weatherRepository.cancel(FORECAST_REQUEST_TAG);
        progressDetailLoading.setVisibility(View.GONE);
    }

    private void fetchWeatherDetails() {
        currentUnit = WeatherPreferences.getUnit(this);
        txtDetailDesc.setText(R.string.detail_loading);
        progressDetailLoading.setVisibility(View.VISIBLE);
        clearForecastUi();
        weatherRepository.fetchWeatherByCity(weatherQuery, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                progressDetailLoading.setVisibility(View.GONE);
                weatherRepository.fetchAirPollution(weatherInfo.getLatitude(), weatherInfo.getLongitude(), weatherInfo, TAG, new WeatherRepository.WeatherCallback() {
                    @Override
                    public void onSuccess(WeatherInfo fullyweatherInfo) {
                        txtDetailCity.setText(displayCityName != null ? displayCityName : fullyweatherInfo.getCityName());
                        txtDetailTemp.setText(Math.round(fullyweatherInfo.getTemperature()) + WeatherPreferences.getTemperatureUnitSymbol(DetailActivity.this));
                        txtDetailDesc.setText(fullyweatherInfo.getDescription());
                        txtHumidity.setText(fullyweatherInfo.getHumidity() + "%");
                        txtWind.setText(String.format(
                                Locale.getDefault(),
                                "%.1f%s",
                                fullyweatherInfo.getWindSpeed(),
                                WeatherPreferences.getWindSpeedSuffix(DetailActivity.this)
                        ));
                        int aqi = fullyweatherInfo.getAqi();
                        if (aqi >= 1 && aqi <= AQI_LEVELS.length) {
                            txtAQI.setText(AQI_LEVELS[aqi - 1]);
                        } else {
                            txtAQI.setText("N/A");
                        }
                        String uv_value = String.valueOf(fullyweatherInfo.getUv());
                        txtUV.setText(uv_value);
                    }

                    @Override
                    public void onError(String message) {
                        showWeatherError(message);
                    }
                });

                loadForecastAfterCurrentWeather(weatherInfo);
            }

            @Override
            public void onError(String message) {
                progressDetailLoading.setVisibility(View.GONE);
                showWeatherError(message);
            }
        });
    }

    private void clearForecastUi() {
        weatherRepository.cancel(FORECAST_REQUEST_TAG);
        layoutForecastSections.setVisibility(View.GONE);
        hourlyForecastItems.clear();
        dailyForecastItems.clear();
        hourlyForecastAdapter.notifyDataSetChanged();
        dailyForecastAdapter.notifyDataSetChanged();
        txtHourlySummary.setText("");
        RecyclerViewHeightHelper.setVerticalListHeightToContent(rvDaily);
    }

    private void loadForecastAfterCurrentWeather(WeatherInfo weatherInfo) {
        weatherRepository.cancel(FORECAST_REQUEST_TAG);
        layoutForecastSections.setVisibility(View.VISIBLE);
        String unitSymbol = WeatherPreferences.getTemperatureUnitSymbol(this);
        txtHourlySummary.setText(getString(
                R.string.main_forecast_hourly_summary_fmt,
                weatherInfo.getDescription(),
                (int) Math.round(weatherInfo.getTemperature()),
                unitSymbol
        ));
        String unit = WeatherPreferences.getUnit(this);
        WeatherRepository.ForecastCallback callback = new WeatherRepository.ForecastCallback() {
            @Override
            public void onSuccess(ForecastResult result) {
                applyForecastResult(weatherInfo, result);
            }

            @Override
            public void onError(String message) {
                // Giữ phần tóm tắt hiện tại; không chặn màn chi tiết
            }
        };
        if (weatherInfo.hasCoordinates()) {
            weatherRepository.fetchForecastByCoordinates(
                    weatherInfo.getLatitude(),
                    weatherInfo.getLongitude(),
                    unit,
                    FORECAST_REQUEST_TAG,
                    callback
            );
        } else {
            weatherRepository.fetchForecastByCity(
                    weatherInfo.getCityName(),
                    unit,
                    FORECAST_REQUEST_TAG,
                    callback
            );
        }
    }

    private void applyForecastResult(WeatherInfo weatherInfo, ForecastResult result) {
        String sym = WeatherPreferences.getTemperatureUnitSymbol(this);
        txtHourlySummary.setText(getString(
                R.string.main_forecast_hourly_summary_fmt,
                weatherInfo.getDescription(),
                result.hourlyMinTempRounded(),
                sym
        ));
        hourlyForecastItems.clear();
        hourlyForecastItems.addAll(result.getHourly());
        hourlyForecastAdapter.notifyDataSetChanged();
        dailyForecastItems.clear();
        dailyForecastItems.addAll(result.getDaily());
        dailyForecastAdapter.notifyDataSetChanged();
        RecyclerViewHeightHelper.setVerticalListHeightToContent(rvDaily);
    }

    private void showWeatherError(String message) {
        progressDetailLoading.setVisibility(View.GONE);
        txtDetailTemp.setText("--");
        txtDetailDesc.setText(message);
        txtHumidity.setText("--%");
        txtWind.setText("--");
        txtAQI.setText("--");
        txtUV.setText("0");
        clearForecastUi();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
