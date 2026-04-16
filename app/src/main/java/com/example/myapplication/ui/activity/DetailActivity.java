package com.example.myapplication.ui.activity;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.data.local.AppDatabase;
import com.example.myapplication.data.local.CachedWeather;
import com.example.myapplication.data.local.CachedWeatherDao;
import com.example.myapplication.data.local.FavoriteCity;
import com.example.myapplication.data.local.FavoriteCityDao;
import com.example.myapplication.data.local.WeatherPreferences;
import com.example.myapplication.data.model.DailyForecast;
import com.example.myapplication.data.model.ForecastResult;
import com.example.myapplication.data.model.HourlyForecast;
import com.example.myapplication.data.model.WeatherInfo;
import com.example.myapplication.data.remote.WeatherRepository;
import com.example.myapplication.ui.adapter.DailyForecastAdapter;
import com.example.myapplication.ui.adapter.HourlyForecastAdapter;
import com.example.myapplication.ui.widget.WeatherAnimationView;
import com.example.myapplication.util.LanguageHelper;
import com.example.myapplication.util.RecyclerViewHeightHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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
    private ImageButton btnFavorite;
    private WeatherAnimationView weatherAnimationView;
    private final List<HourlyForecast> hourlyForecastItems = new ArrayList<>();
    private final List<DailyForecast> dailyForecastItems = new ArrayList<>();
    private HourlyForecastAdapter hourlyForecastAdapter;
    private DailyForecastAdapter dailyForecastAdapter;
    private WeatherRepository weatherRepository;
    private String displayCityName;
    private String weatherQuery;
    private String currentUnit;
    private String currentLanguage;
    private boolean isFavorite;
    private WeatherInfo lastWeatherInfo;

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
        btnFavorite = findViewById(R.id.btnFavorite);
        weatherAnimationView = findViewById(R.id.weatherAnimationView);
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

        btnFavorite.setOnClickListener(v -> toggleFavorite());
        loadFavoriteState();

        findViewById(R.id.cardAqi).setOnClickListener(v -> {
            if (lastWeatherInfo != null && lastWeatherInfo.hasCoordinates()) {
                startActivity(AqiDetailActivity.newIntent(this,
                        lastWeatherInfo.getLatitude(),
                        lastWeatherInfo.getLongitude(),
                        displayCityName));
            }
        });

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
        weatherAnimationView.stopAnimation();
    }

    private void loadFavoriteState() {
        if (displayCityName == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            FavoriteCityDao dao = AppDatabase.getInstance(this).favoriteCityDao();
            boolean fav = dao.countByName(displayCityName) > 0;
            runOnUiThread(() -> {
                isFavorite = fav;
                updateFavoriteIcon();
            });
        });
    }

    private void toggleFavorite() {
        if (displayCityName == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            FavoriteCityDao dao = AppDatabase.getInstance(this).favoriteCityDao();
            if (isFavorite) {
                dao.deleteByName(displayCityName);
                runOnUiThread(() -> {
                    isFavorite = false;
                    updateFavoriteIcon();
                    Toast.makeText(this, R.string.detail_removed_favorite, Toast.LENGTH_SHORT).show();
                });
            } else {
                dao.insert(new FavoriteCity(displayCityName, weatherQuery, System.currentTimeMillis()));
                runOnUiThread(() -> {
                    isFavorite = true;
                    updateFavoriteIcon();
                    Toast.makeText(this, R.string.detail_added_favorite, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateFavoriteIcon() {
        btnFavorite.setImageResource(isFavorite ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
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

                        cacheWeatherData(fullyweatherInfo);
                        lastWeatherInfo = fullyweatherInfo;

                        int hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);
                        boolean isNight = hour < 6 || hour >= 19;
                        weatherAnimationView.setWeather(fullyweatherInfo.getWeatherId(), isNight);
                        weatherAnimationView.startAnimation();
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

    private void cacheWeatherData(WeatherInfo info) {
        if (weatherQuery == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            CachedWeatherDao dao = AppDatabase.getInstance(this).cachedWeatherDao();
            dao.insertOrUpdate(CachedWeather.fromWeatherInfo(weatherQuery, info, currentUnit));
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
