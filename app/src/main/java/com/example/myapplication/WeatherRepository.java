package com.example.myapplication;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class WeatherRepository {

//    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String BASE_URL = "https://api.openweathermap.org/data/3.0/onecall";
    private static final String FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String GEO_DIRECT_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String AIR_POLLUTION_URL = "https://api.openweathermap.org/data/2.5/air_pollution";
    private static final int HOURLY_SLOTS = 8;
    private static final int MAX_DAILY_ROWS = 7;

    public interface WeatherCallback {
        void onSuccess(WeatherInfo weatherInfo);

        void onError(String message);
    }

    public interface ForecastCallback {
        void onSuccess(ForecastResult result);

        void onError(String message);
    }

    public interface AirQualityCallback {
        void onSuccess(AirQualityData data);

        void onError(String message);
    }

    private final RequestQueue requestQueue;
    private final Context appContext;

    public WeatherRepository(Context context) {
        appContext = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(appContext);
    }

    public static boolean hasApiKey() {
        return !TextUtils.isEmpty(BuildConfig.WEATHER_API_KEY.trim());
    }

    public void fetchWeatherByCoordinates(double lat, double lon, String unit, Object requestTag, WeatherCallback callback) {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(lat))
                .appendQueryParameter("lon", String.valueOf(lon))
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .appendQueryParameter("units", unit)
                .appendQueryParameter("lang", LanguageHelper.getCurrentLanguage(appContext))
                .build();
        fetchWeather(uri.toString(), requestTag, callback);
    }

    public void fetchAirPollution(double lat, double lon, WeatherInfo currentInfo, Object requestTag, WeatherCallback callback) {
        Uri uri = Uri.parse(AIR_POLLUTION_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(lat))
                .appendQueryParameter("lon", String.valueOf(lon))
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .build();
        Log.e(TAG, uri.toString());
        // Khởi tạo RequestQueue trực tiếp bằng Context của ứng dụng
        RequestQueue queue = Volley.newRequestQueue(appContext);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri.toString(), null,
                response -> {
                    try {
                        JSONArray list = response.getJSONArray("list");
                        if (list.length() > 0) {
                            int aqi = list.getJSONObject(0).getJSONObject("main").getInt("aqi");
                            currentInfo.setAqi(aqi);
                        }
                        callback.onSuccess(currentInfo);
                    } catch (JSONException e) {
                        callback.onSuccess(currentInfo);
                    }
                },
                error -> callback.onSuccess(currentInfo)
        );

        request.setTag(requestTag);
        queue.add(request); // Thêm vào hàng đợi vừa tạo
    }

    public void fetchAirQualityDetail(double lat, double lon, Object requestTag, AirQualityCallback callback) {
        Uri uri = Uri.parse(AIR_POLLUTION_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(lat))
                .appendQueryParameter("lon", String.valueOf(lon))
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .build();

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, uri.toString(), null,
                response -> {
                    try {
                        JSONArray list = response.getJSONArray("list");
                        if (list.length() > 0) {
                            JSONObject item = list.getJSONObject(0);
                            int aqi = item.getJSONObject("main").getInt("aqi");
                            JSONObject comp = item.getJSONObject("components");
                            AirQualityData data = new AirQualityData(
                                    aqi,
                                    comp.optDouble("co", 0),
                                    comp.optDouble("no", 0),
                                    comp.optDouble("no2", 0),
                                    comp.optDouble("o3", 0),
                                    comp.optDouble("so2", 0),
                                    comp.optDouble("pm2_5", 0),
                                    comp.optDouble("pm10", 0),
                                    comp.optDouble("nh3", 0)
                            );
                            callback.onSuccess(data);
                        } else {
                            callback.onError(appContext.getString(R.string.weather_error_parse));
                        }
                    } catch (JSONException e) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> callback.onError(mapError(error))
        );
        request.setTag(requestTag);
        requestQueue.add(request);
    }

    public void fetchWeatherByCity(String cityName, String unit, Object requestTag, WeatherCallback callback) {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("q", cityName)
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .appendQueryParameter("units", unit)
                .appendQueryParameter("lang", LanguageHelper.getCurrentLanguage(appContext))
                .build();
//        fetchWeatherWithGeoFallback(uri.toString(), cityName, unit, requestTag, callback);
        geocodeDirectThenWeather(cityName, unit, requestTag, callback);
    }

    public void fetchForecastByCoordinates(double lat, double lon, String unit, Object requestTag, ForecastCallback callback) {
        Uri uri = Uri.parse(FORECAST_URL).buildUpon()
                .appendQueryParameter("lat", String.valueOf(lat))
                .appendQueryParameter("lon", String.valueOf(lon))
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .appendQueryParameter("units", unit)
                .appendQueryParameter("lang", LanguageHelper.getCurrentLanguage(appContext))
                .build();
        fetchForecast(uri.toString(), requestTag, callback);
    }

    public void fetchForecastByCity(String cityName, String unit, Object requestTag, ForecastCallback callback) {
        Uri uri = Uri.parse(FORECAST_URL).buildUpon()
                .appendQueryParameter("q", cityName)
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .appendQueryParameter("units", unit)
                .appendQueryParameter("lang", LanguageHelper.getCurrentLanguage(appContext))
                .build();
        fetchForecastWithGeoFallback(uri.toString(), cityName, unit, requestTag, callback);
    }

    public void cancel(Object requestTag) {
        requestQueue.cancelAll(requestTag);
    }

    private void fetchWeather(String url, Object requestTag, WeatherCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        callback.onSuccess(parseWeather(response));
                    } catch (JSONException exception) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                        Log.e(TAG, "message error: " + exception);
                    }
                },
                error -> callback.onError(mapError(error))
        );

        request.setTag(requestTag);
        requestQueue.add(request);
    }

    /**
     * Khi gọi theo tên mà OpenWeather trả 404, thử Geocoding API rồi lấy thời tiết theo tọa độ.
     */
    private void fetchWeatherWithGeoFallback(String url, String cityQuery, String unit, Object requestTag, WeatherCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        callback.onSuccess(parseWeather(response));
                    } catch (JSONException exception) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> {
                    if (isHttpNotFound(error)) {
                        geocodeDirectThenWeather(cityQuery, unit, requestTag, callback);
                    } else {
                        callback.onError(mapError(error));
                    }
                }
        );
        request.setTag(requestTag);
        requestQueue.add(request);
    }

    private void geocodeDirectThenWeather(String cityQuery, String unit, Object requestTag, WeatherCallback callback) {
        Uri geoUri = Uri.parse(GEO_DIRECT_URL).buildUpon()
                .appendQueryParameter("q", cityQuery)
                .appendQueryParameter("limit", "5")
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .build();
        JsonArrayRequest geoRequest = new JsonArrayRequest(
                Request.Method.GET,
                geoUri.toString(),
                null,
                response -> {
                    try {
                        if (response.length() == 0) {
                            callback.onError(appContext.getString(R.string.weather_error_not_found));
                            return;
                        }
                        JSONObject first = response.getJSONObject(0);
                        double lat = first.getDouble("lat");
                        double lon = first.getDouble("lon");
                        fetchWeatherByCoordinates(lat, lon, unit, requestTag, callback);
                    } catch (JSONException e) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> callback.onError(mapError(error))
        );
        geoRequest.setTag(requestTag);
        requestQueue.add(geoRequest);
    }

    private void fetchForecast(String url, Object requestTag, ForecastCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        callback.onSuccess(parseForecast(response));
                    } catch (JSONException exception) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> callback.onError(mapError(error))
        );
        request.setTag(requestTag);
        requestQueue.add(request);
    }

    private void fetchForecastWithGeoFallback(String url, String cityQuery, String unit, Object requestTag, ForecastCallback callback) {
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        callback.onSuccess(parseForecast(response));
                    } catch (JSONException exception) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> {
                    if (isHttpNotFound(error)) {
                        geocodeDirectThenForecast(cityQuery, unit, requestTag, callback);
                    } else {
                        callback.onError(mapError(error));
                    }
                }
        );
        request.setTag(requestTag);
        requestQueue.add(request);
    }

    private void geocodeDirectThenForecast(String cityQuery, String unit, Object requestTag, ForecastCallback callback) {
        Uri geoUri = Uri.parse(GEO_DIRECT_URL).buildUpon()
                .appendQueryParameter("q", cityQuery)
                .appendQueryParameter("limit", "5")
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .build();
        JsonArrayRequest geoRequest = new JsonArrayRequest(
                Request.Method.GET,
                geoUri.toString(),
                null,
                response -> {
                    try {
                        if (response.length() == 0) {
                            callback.onError(appContext.getString(R.string.weather_error_not_found));
                            return;
                        }
                        JSONObject first = response.getJSONObject(0);
                        double lat = first.getDouble("lat");
                        double lon = first.getDouble("lon");
                        fetchForecastByCoordinates(lat, lon, unit, requestTag, callback);
                    } catch (JSONException e) {
                        callback.onError(appContext.getString(R.string.weather_error_parse));
                    }
                },
                error -> callback.onError(mapError(error))
        );
        geoRequest.setTag(requestTag);
        requestQueue.add(geoRequest);
    }

    private static boolean isHttpNotFound(VolleyError error) {
        return error.networkResponse != null && error.networkResponse.statusCode == 404;
    }

    public String getCityName(double lat, double lon) {
        String cityName = "Unknown";
        Geocoder geocoder = new Geocoder(appContext, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
            if (addresses != null && addresses.size() > 0) {
                // Lấy tên thành phố hoặc khu vực hành chính
                cityName = addresses.get(0).getLocality();
                if (cityName == null) {
                    cityName = addresses.get(0).getAdminArea();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }

    private WeatherInfo parseWeather(JSONObject response) throws JSONException {
//        String cityName = response.optString("name", appContext.getString(R.string.weather_unknown_location));
        double latitude = response.optDouble("lat");
        double longitude = response.optDouble("lon");
        boolean hasCoordinates = false;
        String cityName = getCityName(latitude, longitude);
//        JSONObject main = response.getJSONObject("main");
        JSONObject current = response.getJSONObject("current");
        if(!response.isNull("lat") && !response.isNull("lon")){
            hasCoordinates = true;
        }
        double temperature = current.getDouble("temp");
        int humidity = current.optInt("humidity", 0);
        double uv = current.optDouble("uvi", 0.0);
        double windSpeed = current.optDouble("wind_speed", 0.0);

        // One Call 3.x: weather nằm trong "current"; API 2.5 current weather: "weather" ở root
        JSONArray weatherArray = current.optJSONArray("weather");
        if (weatherArray == null || weatherArray.length() == 0) {
            weatherArray = response.optJSONArray("weather");
        }
        String description = "";
        int weatherId = 800;
        if (weatherArray != null && weatherArray.length() > 0) {
            JSONObject w = weatherArray.getJSONObject(0);
            description = w.optString("description", "");
            weatherId = w.optInt("id", 800);
            if (TextUtils.isEmpty(description)) {
                description = w.optString("main", "");
            }
        }

        int aqi = 0;

        return new WeatherInfo(
                cityName,
                temperature,
                capitalizeDescription(description),
                humidity,
                windSpeed,
                aqi,
                uv,
                latitude,
                longitude,
                hasCoordinates,
                weatherId
        );
    }

    private ForecastResult parseForecast(JSONObject response) throws JSONException {
        JSONArray list = response.getJSONArray("list");
        Locale locale = new Locale(LanguageHelper.getCurrentLanguage(appContext), "");
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", locale);
        hourFormat.setTimeZone(TimeZone.getDefault());

        List<HourlyForecast> hourly = new ArrayList<>();
        int hourlyCount = Math.min(HOURLY_SLOTS, list.length());
        for (int i = 0; i < hourlyCount; i++) {
            JSONObject item = list.getJSONObject(i);
            long dtSec = item.getLong("dt");
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(dtSec * 1000L);
            String timeLabel = hourFormat.format(cal.getTime());
            double temp = item.getJSONObject("main").getDouble("temp");
            int pop = (int) Math.round(item.optDouble("pop", 0) * 100.0);
            String icon = firstWeatherIcon(item);
            hourly.add(new HourlyForecast(timeLabel, temp, pop, icon));
        }

        Map<String, DayAccumulator> dayMap = new LinkedHashMap<>();
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            long dtSec = item.getLong("dt");
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(dtSec * 1000L);
            String dayKey = dayKey(cal);
            DayAccumulator acc = dayMap.get(dayKey);
            if (acc == null) {
                acc = new DayAccumulator(midnightMillis(cal));
                dayMap.put(dayKey, acc);
            }
            double temp = item.getJSONObject("main").getDouble("temp");
            double pop = item.optDouble("pop", 0);
            String icon = firstWeatherIcon(item);
            int hour = cal.get(Calendar.HOUR_OF_DAY);
            acc.addSample(temp, pop, icon, hour);
        }

        List<DailyForecast> daily = new ArrayList<>();
        for (DayAccumulator acc : dayMap.values()) {
            if (daily.size() >= MAX_DAILY_ROWS) {
                break;
            }
            daily.add(acc.toDailyForecast(appContext));
        }

        return new ForecastResult(hourly, daily);
    }

    private static String dayKey(Calendar cal) {
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.MONTH) + "-" + cal.get(Calendar.DAY_OF_MONTH);
    }

    private static long midnightMillis(Calendar source) {
        Calendar c = Calendar.getInstance(source.getTimeZone());
        c.setTimeInMillis(source.getTimeInMillis());
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private static String firstWeatherIcon(JSONObject item) throws JSONException {
        JSONArray weatherArray = item.optJSONArray("weather");
        if (weatherArray != null && weatherArray.length() > 0) {
            return weatherArray.getJSONObject(0).optString("icon", "01d");
        }
        return "01d";
    }

    private static final class DayAccumulator {

        private final long dayStartMillis;
        private double minTemp = Double.MAX_VALUE;
        private double maxTemp = -Double.MAX_VALUE;
        private double maxPop;
        private String iconDay = "02d";
        private String iconNight = "02n";
        private int bestDayDist = Integer.MAX_VALUE;
        private int bestNightDist = Integer.MAX_VALUE;

        DayAccumulator(long dayStartMillis) {
            this.dayStartMillis = dayStartMillis;
        }

        void addSample(double temp, double pop, String icon, int hour) {
            minTemp = Math.min(minTemp, temp);
            maxTemp = Math.max(maxTemp, temp);
            maxPop = Math.max(maxPop, pop);

            if (hour >= 6 && hour <= 18) {
                int dist = Math.abs(hour - 12);
                if (dist < bestDayDist) {
                    bestDayDist = dist;
                    iconDay = icon;
                }
            }
            int nightDist = nightDistance(hour);
            if (nightDist < bestNightDist) {
                bestNightDist = nightDist;
                iconNight = icon;
            }
        }

        private static int nightDistance(int hour) {
            if (hour >= 18) {
                return Math.abs(hour - 21);
            }
            return Math.abs(hour - 3);
        }

        DailyForecast toDailyForecast(Context context) {
            String label = formatDayLabel(context, dayStartMillis);
            int popPct = (int) Math.round(maxPop * 100.0);
            return new DailyForecast(
                    label,
                    popPct,
                    iconDay,
                    iconNight,
                    (int) Math.round(maxTemp),
                    (int) Math.round(minTemp)
            );
        }
    }

    private static String formatDayLabel(Context context, long dayStartMillis) {
        Calendar day = Calendar.getInstance(TimeZone.getDefault());
        day.setTimeInMillis(dayStartMillis);
        stripToMidnight(day);
        Calendar today = Calendar.getInstance(TimeZone.getDefault());
        stripToMidnight(today);
        long diffDays = (day.getTimeInMillis() - today.getTimeInMillis()) / (24L * 60L * 60L * 1000L);
        if (diffDays == 0) {
            return context.getString(R.string.main_forecast_today);
        }
        if (diffDays == 1) {
            return context.getString(R.string.main_forecast_tomorrow);
        }
        String lang = LanguageHelper.getCurrentLanguage(context);
        if (LanguageHelper.LANGUAGE_VIETNAMESE.equals(lang)) {
            return vietnameseShortWeekday(day);
        }
        Locale locale = Locale.ENGLISH;
        SimpleDateFormat sdf = new SimpleDateFormat("EEE", locale);
        sdf.setTimeZone(TimeZone.getDefault());
        return sdf.format(day.getTime());
    }

    private static void stripToMidnight(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static String vietnameseShortWeekday(Calendar cal) {
        int dow = cal.get(Calendar.DAY_OF_WEEK);
        switch (dow) {
            case Calendar.MONDAY:
                return "T.2";
            case Calendar.TUESDAY:
                return "T.3";
            case Calendar.WEDNESDAY:
                return "T.4";
            case Calendar.THURSDAY:
                return "T.5";
            case Calendar.FRIDAY:
                return "T.6";
            case Calendar.SATURDAY:
                return "T.7";
            case Calendar.SUNDAY:
            default:
                return "CN";
        }
    }

    private String mapError(VolleyError error) {
        if (error instanceof AuthFailureError) {
            return appContext.getString(R.string.weather_error_invalid_key);
        }
        if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            return appContext.getString(R.string.weather_error_connection);
        }
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            if (statusCode == 401) {
                return appContext.getString(R.string.weather_error_invalid_key);
            }
            if (statusCode == 404) {
                return appContext.getString(R.string.weather_error_not_found);
            }
        }
        return appContext.getString(R.string.weather_error_generic);
    }

    private String capitalizeDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return appContext.getString(R.string.weather_no_description);
        }
        return description.substring(0, 1).toUpperCase(Locale.getDefault()) + description.substring(1);
    }
}
