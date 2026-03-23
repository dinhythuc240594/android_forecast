package com.example.myapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String WEATHER_REQUEST_TAG = "main_weather";
    private static final String FORECAST_REQUEST_TAG = "main_forecast";
    private static final long LOCATION_TIMEOUT_MS = 10000L;
    private static final String DEFAULT_CITY = "Hanoi";

    private TextView txtStatus;
    private TextView txtCity;
    private TextView txtTemp;
    private TextView txtDesc;
    private TextView txtHelper;
    private TextView txtHumidityValue;
    private TextView txtWindValue;
    private SwipeRefreshLayout swipeRefresh;
    private LinearProgressIndicator progressLoading;
    private View layoutForecastSections;
    private TextView txtHourlySummary;
    private RecyclerView rvHourly;
    private RecyclerView rvDaily;
    private final List<HourlyForecast> hourlyForecastItems = new ArrayList<>();
    private final List<DailyForecast> dailyForecastItems = new ArrayList<>();
    private HourlyForecastAdapter hourlyForecastAdapter;
    private DailyForecastAdapter dailyForecastAdapter;
    private FusedLocationProviderClient fusedLocationClient;
    private WeatherRepository weatherRepository;
    private Location lastKnownLocation;
    private String currentUnit;
    private String currentLanguage;
    private boolean showingFallbackCity;
    private CancellationTokenSource locationCancellationTokenSource;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable locationTimeoutRunnable = this::handleLocationTimeout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        LanguageHelper.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        txtStatus = findViewById(R.id.txtStatus);
        txtCity = findViewById(R.id.txtCity);
        txtTemp = findViewById(R.id.txtTemp);
        txtDesc = findViewById(R.id.txtDesc);
        txtHelper = findViewById(R.id.txtHelper);
        txtHumidityValue = findViewById(R.id.txtHumidityValue);
        txtWindValue = findViewById(R.id.txtWindValue);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        progressLoading = findViewById(R.id.progressLoading);
        layoutForecastSections = findViewById(R.id.layoutForecastSections);
        txtHourlySummary = findViewById(R.id.txtHourlySummary);
        rvHourly = findViewById(R.id.rvHourly);
        rvDaily = findViewById(R.id.rvDaily);

        hourlyForecastAdapter = new HourlyForecastAdapter(hourlyForecastItems);
        rvHourly.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvHourly.setAdapter(hourlyForecastAdapter);
        rvHourly.setHasFixedSize(true);

        dailyForecastAdapter = new DailyForecastAdapter(dailyForecastItems);
        rvDaily.setLayoutManager(new LinearLayoutManager(this));
        rvDaily.setAdapter(dailyForecastAdapter);

        swipeRefresh.setColorSchemeColors(
                ContextCompat.getColor(this, R.color.purple_700),
                ContextCompat.getColor(this, R.color.teal_200));
        swipeRefresh.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefresh.setOnRefreshListener(this::performRefresh);
        Button btnGoToList = findViewById(R.id.btnGoToList);
        Button btnSettings = findViewById(R.id.btnSettings);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        weatherRepository = new WeatherRepository(this);
        currentUnit = WeatherPreferences.getUnit(this);
        currentLanguage = LanguageHelper.getCurrentLanguage(this);

        btnGoToList.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, CityListActivity.class)));

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        if (WeatherRepository.hasApiKey()) {
            getLocation();
        } else {
            showMissingApiKey();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String selectedUnit = WeatherPreferences.getUnit(this);
        String selectedLanguage = LanguageHelper.getCurrentLanguage(this);
        if (!selectedUnit.equals(currentUnit) || !selectedLanguage.equals(currentLanguage)) {
            currentUnit = selectedUnit;
            currentLanguage = selectedLanguage;
            if (lastKnownLocation != null) {
                fetchWeatherData(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
            } else if (showingFallbackCity) {
                fetchFallbackWeather(false, null);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        weatherRepository.cancel(WEATHER_REQUEST_TAG);
        weatherRepository.cancel(FORECAST_REQUEST_TAG);
        cancelLocationTimeout();
        swipeRefresh.setRefreshing(false);
    }

    private void performRefresh() {
        if (!WeatherRepository.hasApiKey()) {
            swipeRefresh.setRefreshing(false);
            showMissingApiKey();
            return;
        }
        if (!swipeRefresh.isRefreshing()) {
            swipeRefresh.setRefreshing(true);
        }
        getLocation();
    }

    private void fetchWeatherData(double lat, double lon) {
        currentUnit = WeatherPreferences.getUnit(this);
        weatherRepository.cancel(WEATHER_REQUEST_TAG);
        showLoadingState(
                R.string.main_status_loading_weather,
                getString(R.string.main_loading_city),
                R.string.main_helper_syncing
        );

        weatherRepository.fetchWeatherByCoordinates(lat, lon, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                showingFallbackCity = false;
                updateWeatherUi(weatherInfo);
            }

            @Override
            public void onError(String message) {
                showWeatherError(message);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (hasGrantedLocationPermission(grantResults)) {
                getLocation();
            } else {
                fetchFallbackWeather(true, getString(R.string.main_message_permission_denied));
            }
        }
    }

    private void getLocation() {
        if (!hasLocationPermission()) {
            swipeRefresh.setRefreshing(false);
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        if (!isLocationEnabled()) {
            fetchFallbackWeather(true, getString(R.string.main_message_location_disabled));
            return;
        }

        showLoadingState(
                R.string.main_status_loading_location,
                getString(R.string.main_loading_city),
                R.string.main_helper_locating
        );
        startLocationTimeout();
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        handleLocationSuccess(location);
                    } else {
                        requestCurrentLocation();
                    }
                })
                .addOnFailureListener(exception ->
                        fetchFallbackWeather(true, getString(R.string.main_message_location_unavailable)));
    }

    @SuppressLint("MissingPermission")
    private void requestCurrentLocation() {
        locationCancellationTokenSource = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        locationCancellationTokenSource.getToken()
                )
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        handleLocationSuccess(location);
                    } else {
                        fetchFallbackWeather(true, getString(R.string.main_message_gps_unavailable));
                    }
                })
                .addOnFailureListener(exception ->
                        fetchFallbackWeather(true, getString(R.string.main_message_location_unavailable)));
    }

    private void handleLocationSuccess(Location location) {
        cancelLocationTimeout();
        showingFallbackCity = false;
        lastKnownLocation = location;
        fetchWeatherData(location.getLatitude(), location.getLongitude());
    }

    private void fetchFallbackWeather(boolean shouldShowToast, @Nullable String message) {
        cancelLocationTimeout();
        lastKnownLocation = null;
        showingFallbackCity = true;
        currentUnit = WeatherPreferences.getUnit(this);
        weatherRepository.cancel(WEATHER_REQUEST_TAG);

        if (shouldShowToast && message != null) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }

        showLoadingState(
                R.string.main_status_fallback,
                getString(R.string.main_loading_fallback_city),
                R.string.main_helper_fallback
        );

        weatherRepository.fetchWeatherByCity(DEFAULT_CITY, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                showingFallbackCity = true;
                updateWeatherUi(weatherInfo);
            }

            @Override
            public void onError(String errorMessage) {
                showWeatherError(errorMessage);
            }
        });
    }

    private void startLocationTimeout() {
        cancelLocationTimeout();
        mainHandler.postDelayed(locationTimeoutRunnable, LOCATION_TIMEOUT_MS);
    }

    private void cancelLocationTimeout() {
        mainHandler.removeCallbacks(locationTimeoutRunnable);
        if (locationCancellationTokenSource != null) {
            locationCancellationTokenSource.cancel();
            locationCancellationTokenSource = null;
        }
    }

    private void handleLocationTimeout() {
        fetchFallbackWeather(true, getString(R.string.main_message_location_timeout));
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean hasGrantedLocationPermission(int[] grantResults) {
        for (int grantResult : grantResults) {
            if (grantResult == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }

    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && LocationManagerCompat.isLocationEnabled(locationManager);
    }

    private void showLoadingState(int statusResId, String cityText, int helperResId) {
        txtStatus.setText(statusResId);
        txtCity.setText(cityText);
        txtTemp.setText("--");
        txtDesc.setText(R.string.main_weather_placeholder);
        txtHelper.setText(helperResId);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        progressLoading.setVisibility(View.VISIBLE);
        clearForecastUi();
    }

    private void updateWeatherUi(WeatherInfo weatherInfo) {
        txtStatus.setText(showingFallbackCity ? R.string.main_status_fallback : R.string.main_status_current_location);
        txtCity.setText(weatherInfo.getCityName());
        txtTemp.setText(Math.round(weatherInfo.getTemperature()) + WeatherPreferences.getTemperatureUnitSymbol(this));
        txtDesc.setText(weatherInfo.getDescription());
        txtHelper.setText(showingFallbackCity ? R.string.main_helper_fallback : R.string.main_helper_current_location);
        txtHumidityValue.setText(weatherInfo.getHumidity() + "%");
        txtWindValue.setText(String.format(
                Locale.getDefault(),
                "%.1f%s",
                weatherInfo.getWindSpeed(),
                WeatherPreferences.getWindSpeedSuffix(this)
        ));
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        loadForecastAfterCurrentWeather(weatherInfo);
    }

    private void showMissingApiKey() {
        txtStatus.setText(R.string.main_status_missing_config);
        txtCity.setText(R.string.main_status_missing_config);
        txtTemp.setText("--");
        txtDesc.setText(R.string.main_helper_missing_api_key);
        txtHelper.setText(R.string.main_helper_missing_api_key);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        clearForecastUi();
        Toast.makeText(this, R.string.main_message_missing_api_key, Toast.LENGTH_LONG).show();
    }

    private void showWeatherError(String message) {
        txtStatus.setText(R.string.main_status_error);
        txtCity.setText(R.string.main_status_error);
        txtTemp.setText("--");
        txtDesc.setText(message);
        txtHelper.setText(R.string.main_helper_retry);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        clearForecastUi();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void clearForecastUi() {
        weatherRepository.cancel(FORECAST_REQUEST_TAG);
        layoutForecastSections.setVisibility(View.GONE);
        hourlyForecastItems.clear();
        dailyForecastItems.clear();
        hourlyForecastAdapter.notifyDataSetChanged();
        dailyForecastAdapter.notifyDataSetChanged();
        txtHourlySummary.setText("");
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
                // Giữ dự báo cũ nếu có; không làm gián đoạn luồng chính
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
    }
}
