package com.example.myapplication.ui.activity;

import static android.content.ContentValues.TAG;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewParent;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.myapplication.R;
import com.example.myapplication.data.local.AppDatabase;
import com.example.myapplication.data.local.CachedWeather;
import com.example.myapplication.data.local.CachedWeatherDao;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.progressindicator.LinearProgressIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String WEATHER_REQUEST_TAG = "main_weather";
    private static final String FORECAST_REQUEST_TAG = "main_forecast";
    private static final long LOCATION_TIMEOUT_MS = 10000L;
    private static final long BACKGROUND_TIME_CHECK_MS = 60_000L;
    private static final String DEFAULT_CITY = "Hanoi";

    private static final String[] AQI_LEVELS = {"Tốt", "Khá", "Trung bình", "Kém", "Rất kém"};

    private TextView txtStatus;
    private TextView txtCity;
    private TextView txtCitySticky;
    private TextView txtTemp;
    private TextView txtDesc;
    private TextView txtHumidityValue;
    private TextView txtWindValue;
    private TextView txtAQIValue;
    private TextView txtUVValue;

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
    private final Runnable backgroundTimeRunnable = this::tickDayNightBackground;

    private View backgroundDay;
    private View backgroundNight;
    private View backgroundShimmer;
    private View backgroundScrim;
    private ObjectAnimator shimmerAnimator;
    private Boolean lastDayMode;

    private NestedScrollView nestedScrollMain;
    private View layoutCityRow;
    private View scrollContentRoot;
    private View appBarStickyCity;
    private int cityStickyScrollThresholdPx;
    private boolean stickyHeaderVisible;
    private int stickyScrollSlopPx;

    private WeatherAnimationView weatherAnimationView;
    private int currentWeatherId = 800;
    private WeatherInfo lastWeatherInfo;

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
        txtCitySticky = findViewById(R.id.txtCitySticky);
        nestedScrollMain = findViewById(R.id.nestedScrollMain);
        layoutCityRow = findViewById(R.id.layoutCityRow);
        scrollContentRoot = findViewById(R.id.scrollContentRoot);
        appBarStickyCity = findViewById(R.id.appBarStickyCity);
        stickyScrollSlopPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
        txtTemp = findViewById(R.id.txtTemp);
        txtDesc = findViewById(R.id.txtDesc);
        txtHumidityValue = findViewById(R.id.txtHumidityValue);
        txtWindValue = findViewById(R.id.txtWindValue);
        txtAQIValue = findViewById(R.id.txtAQI);
        txtUVValue = findViewById(R.id.txtUV);
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
        Button btnSettings = findViewById(R.id.btnSettings);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        weatherRepository = new WeatherRepository(this);
        currentUnit = WeatherPreferences.getUnit(this);
        currentLanguage = LanguageHelper.getCurrentLanguage(this);

        findViewById(R.id.btnSearchCity).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CityListActivity.class);
            intent.putExtra(CityListActivity.EXTRA_FOCUS_SEARCH, true);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, SettingsActivity.class)));

        setupStickyCityHeader();

        setupDynamicBackground();

        weatherAnimationView = findViewById(R.id.weatherAnimationView);

        findViewById(R.id.cardAqi).setOnClickListener(v -> {
            if (lastWeatherInfo != null && lastWeatherInfo.hasCoordinates()) {
                startActivity(AqiDetailActivity.newIntent(this,
                        lastWeatherInfo.getLatitude(),
                        lastWeatherInfo.getLongitude(),
                        lastWeatherInfo.getCityName()));
            }
        });

        if (WeatherRepository.hasApiKey()) {
            getLocation();
        } else {
            showMissingApiKey();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean nowDay = isLocalDayTime();
        if (lastDayMode == null || lastDayMode != nowDay) {
            boolean animate = lastDayMode != null;
            lastDayMode = nowDay;
            applyDayNightBackground(nowDay, animate);
            startShimmerAnimation(nowDay);
        } else {
            startShimmerAnimation(nowDay);
        }
        scheduleBackgroundTimeCheck();
        weatherAnimationView.setWeather(currentWeatherId, !nowDay);
        weatherAnimationView.startAnimation();

        nestedScrollMain.post(() -> {
            remeasureCityStickyThreshold();
            updateStickyCityHeaderVisibility(nestedScrollMain.getScrollY());
        });

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
        stopBackgroundUpdates();
        weatherAnimationView.stopAnimation();
    }

    private void setupStickyCityHeader() {
        nestedScrollMain.post(() -> {
            remeasureCityStickyThreshold();
            updateStickyCityHeaderVisibility(nestedScrollMain.getScrollY());
        });
        nestedScrollMain.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateStickyCityHeaderVisibility(scrollY);
            }
        });
    }

    private void remeasureCityStickyThreshold() {
        if (scrollContentRoot == null || layoutCityRow == null) {
            return;
        }
        cityStickyScrollThresholdPx = topRelativeTo(layoutCityRow, scrollContentRoot);
    }

    private void updateStickyCityHeaderVisibility(int scrollY) {
        if (cityStickyScrollThresholdPx <= 0) {
            remeasureCityStickyThreshold();
        }
        if (cityStickyScrollThresholdPx <= 0) {
            return;
        }
        boolean show = scrollY > cityStickyScrollThresholdPx - stickyScrollSlopPx;
        if (show != stickyHeaderVisible) {
            stickyHeaderVisible = show;
            appBarStickyCity.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * Khoảng cách dọc từ {@code view} tới {@code ancestor} (cùng cây view).
     */
    private static int topRelativeTo(View view, View ancestor) {
        int top = 0;
        View v = view;
        while (v != null && v != ancestor) {
            top += v.getTop();
            ViewParent p = v.getParent();
            v = p instanceof View ? (View) p : null;
        }
        return top;
    }

    private void setCityText(CharSequence text) {
        txtCity.setText(text);
        txtCitySticky.setText(text);
    }

    private void setCityText(int resId) {
        txtCity.setText(resId);
        txtCitySticky.setText(resId);
    }

    private void setupDynamicBackground() {
        backgroundDay = findViewById(R.id.backgroundDay);
        backgroundNight = findViewById(R.id.backgroundNight);
        backgroundShimmer = findViewById(R.id.backgroundShimmer);
        backgroundScrim = findViewById(R.id.backgroundScrim);
        boolean day = isLocalDayTime();
        lastDayMode = day;
        applyDayNightBackground(day, false);
        startShimmerAnimation(day);
    }

    /**
     * Giờ địa phương: 6:00–18:59 = ban ngày (nền sáng), còn lại = ban đêm (nền tối).
     */
    private boolean isLocalDayTime() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return hour >= 6 && hour < 19;
    }

    private void applyDayNightBackground(boolean day, boolean animate) {
        if (animate) {
            backgroundDay.animate().alpha(day ? 1f : 0f).setDuration(900).start();
            backgroundNight.animate().alpha(day ? 0f : 1f).setDuration(900).start();
        } else {
            backgroundDay.setAlpha(day ? 1f : 0f);
            backgroundNight.setAlpha(day ? 0f : 1f);
        }
        backgroundShimmer.setBackgroundResource(
                day ? R.drawable.bg_main_day_shimmer : R.drawable.bg_main_night_shimmer);
        backgroundScrim.setBackgroundColor(
                ContextCompat.getColor(this, day ? R.color.home_scrim_day : R.color.home_scrim_night));
    }

    private void startShimmerAnimation(boolean day) {
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
        float lo = day ? 0.12f : 0.08f;
        float hi = day ? 0.45f : 0.34f;
        shimmerAnimator = ObjectAnimator.ofFloat(backgroundShimmer, View.ALPHA, lo, hi);
        shimmerAnimator.setDuration(4200);
        shimmerAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        shimmerAnimator.start();
    }

    private void tickDayNightBackground() {
        boolean now = isLocalDayTime();
        if (lastDayMode == null || lastDayMode != now) {
            lastDayMode = now;
            applyDayNightBackground(now, true);
            startShimmerAnimation(now);
        }
        mainHandler.postDelayed(backgroundTimeRunnable, BACKGROUND_TIME_CHECK_MS);
    }

    private void scheduleBackgroundTimeCheck() {
        mainHandler.removeCallbacks(backgroundTimeRunnable);
        mainHandler.post(backgroundTimeRunnable);
    }

    private void stopBackgroundUpdates() {
        mainHandler.removeCallbacks(backgroundTimeRunnable);
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
            shimmerAnimator = null;
        }
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
                getString(R.string.main_loading_city)
        );

        weatherRepository.fetchWeatherByCoordinates(lat, lon, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                showingFallbackCity = false;

                weatherRepository.fetchAirPollution(lat, lon, weatherInfo, TAG, new WeatherRepository.WeatherCallback() {
                    @Override
                    public void onSuccess(WeatherInfo fullyLoadedInfo) {
                        updateWeatherUi(fullyLoadedInfo);
                    }

                    @Override
                    public void onError(String message) {
                        showWeatherError(message);
                    }
                });
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
                getString(R.string.main_loading_city)
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
                getString(R.string.main_loading_fallback_city)
        );

        weatherRepository.fetchWeatherByCity(DEFAULT_CITY, currentUnit, WEATHER_REQUEST_TAG, new WeatherRepository.WeatherCallback() {
            @Override
            public void onSuccess(WeatherInfo weatherInfo) {
                showingFallbackCity = true;

                weatherRepository.fetchAirPollution(weatherInfo.getLatitude(), weatherInfo.getLongitude(), weatherInfo, TAG, new WeatherRepository.WeatherCallback() {
                    @Override
                    public void onSuccess(WeatherInfo fullyLoadedInfo) {
                        updateWeatherUi(fullyLoadedInfo);
                    }

                    @Override
                    public void onError(String message) {
                        showWeatherError(message);
                    }
                });
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

    private void showLoadingState(int statusResId, String cityText) {
        txtStatus.setText(statusResId);
        setCityText(cityText);
        txtTemp.setText("--");
        txtDesc.setText(R.string.main_weather_placeholder);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        txtAQIValue.setText("--");
        txtUVValue.setText("--");
        progressLoading.setVisibility(View.VISIBLE);
        clearForecastUi();
    }

    private void updateWeatherUi(WeatherInfo weatherInfo) {
        lastWeatherInfo = weatherInfo;
        txtStatus.setText(showingFallbackCity ? R.string.main_status_fallback : R.string.main_status_current_location);
        setCityText(weatherInfo.getCityName());
        txtTemp.setText(Math.round(weatherInfo.getTemperature()) + WeatherPreferences.getTemperatureUnitSymbol(this));
        txtDesc.setText(weatherInfo.getDescription());
        txtHumidityValue.setText(weatherInfo.getHumidity() + "%");
        txtWindValue.setText(String.format(
                Locale.getDefault(),
                "%.1f%s",
                weatherInfo.getWindSpeed(),
                WeatherPreferences.getWindSpeedSuffix(this)
        ));
        int aqi = weatherInfo.getAqi();
        if (aqi >= 1 && aqi <= AQI_LEVELS.length) {
            txtAQIValue.setText(AQI_LEVELS[aqi - 1]);
        } else {
            txtAQIValue.setText("N/A");
        }
        String uv_value = String.valueOf(weatherInfo.getUv());
        txtUVValue.setText(uv_value);

        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);

        currentWeatherId = weatherInfo.getWeatherId();
        weatherAnimationView.setWeather(currentWeatherId, !isLocalDayTime());
        weatherAnimationView.startAnimation();

        loadForecastAfterCurrentWeather(weatherInfo);
        cacheCurrentWeather(weatherInfo);
    }

    private void cacheCurrentWeather(WeatherInfo info) {
        String key = showingFallbackCity ? DEFAULT_CITY : "current_location";
        Executors.newSingleThreadExecutor().execute(() -> {
            CachedWeatherDao dao = AppDatabase.getInstance(this).cachedWeatherDao();
            dao.insertOrUpdate(CachedWeather.fromWeatherInfo(key, info, currentUnit));
            long oneDayAgo = System.currentTimeMillis() - 24L * 60L * 60L * 1000L;
            dao.deleteOlderThan(oneDayAgo);
        });
    }

    private void showCachedWeather() {
        String key = showingFallbackCity ? DEFAULT_CITY : "current_location";
        String unit = WeatherPreferences.getUnit(this);
        Executors.newSingleThreadExecutor().execute(() -> {
            CachedWeatherDao dao = AppDatabase.getInstance(this).cachedWeatherDao();
            CachedWeather cached = dao.get(key, unit);
            if (cached != null) {
                WeatherInfo info = cached.toWeatherInfo();
                long updatedAt = cached.updatedAt;
                runOnUiThread(() -> {
                    txtStatus.setText(R.string.main_status_cached);
                    setCityText(info.getCityName());
                    txtTemp.setText(Math.round(info.getTemperature()) + WeatherPreferences.getTemperatureUnitSymbol(this));
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    String timeStr = sdf.format(new Date(updatedAt));
                    txtDesc.setText(getString(R.string.main_cached_time_fmt, timeStr));
                    txtHumidityValue.setText(info.getHumidity() + "%");
                    txtWindValue.setText(String.format(
                            Locale.getDefault(),
                            "%.1f%s",
                            info.getWindSpeed(),
                            WeatherPreferences.getWindSpeedSuffix(this)
                    ));
                    int aqi = info.getAqi();
                    if (aqi >= 1 && aqi <= AQI_LEVELS.length) {
                        txtAQIValue.setText(AQI_LEVELS[aqi - 1]);
                    } else {
                        txtAQIValue.setText("N/A");
                    }
                    txtUVValue.setText(String.valueOf(info.getUv()));
                    progressLoading.setVisibility(View.GONE);
                    swipeRefresh.setRefreshing(false);
                    currentWeatherId = info.getWeatherId();
                    weatherAnimationView.setWeather(currentWeatherId, !isLocalDayTime());
                    weatherAnimationView.startAnimation();
                });
            }
        });
    }

    private void showMissingApiKey() {
        txtStatus.setText(R.string.main_status_missing_config);
        setCityText(R.string.main_status_missing_config);
        txtTemp.setText("--");
        txtDesc.setText(R.string.main_helper_missing_api_key);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        clearForecastUi();
        Toast.makeText(this, R.string.main_message_missing_api_key, Toast.LENGTH_LONG).show();
    }

    private void showWeatherError(String message) {
        txtStatus.setText(R.string.main_status_error);
        setCityText(R.string.main_status_error);
        txtTemp.setText("--");
        txtDesc.setText(message);
        txtHumidityValue.setText("--%");
        txtWindValue.setText("--");
        progressLoading.setVisibility(View.GONE);
        swipeRefresh.setRefreshing(false);
        clearForecastUi();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        showCachedWeather();
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
}
