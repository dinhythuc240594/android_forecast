package com.example.myapplication;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class WeatherRepository {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/weather";

    public interface WeatherCallback {
        void onSuccess(WeatherInfo weatherInfo);

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

    public void fetchWeatherByCity(String cityName, String unit, Object requestTag, WeatherCallback callback) {
        Uri uri = Uri.parse(BASE_URL).buildUpon()
                .appendQueryParameter("q", cityName)
                .appendQueryParameter("appid", BuildConfig.WEATHER_API_KEY)
                .appendQueryParameter("units", unit)
                .appendQueryParameter("lang", LanguageHelper.getCurrentLanguage(appContext))
                .build();
        fetchWeather(uri.toString(), requestTag, callback);
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
                    }
                },
                error -> callback.onError(mapError(error))
        );

        request.setTag(requestTag);
        requestQueue.add(request);
    }

    private WeatherInfo parseWeather(JSONObject response) throws JSONException {
        String cityName = response.optString("name", appContext.getString(R.string.weather_unknown_location));
        JSONObject main = response.getJSONObject("main");
        double temperature = main.getDouble("temp");
        int humidity = main.optInt("humidity", 0);

        JSONArray weatherArray = response.optJSONArray("weather");
        String description = "";
        if (weatherArray != null && weatherArray.length() > 0) {
            description = weatherArray.getJSONObject(0).optString("description", "");
        }

        JSONObject wind = response.optJSONObject("wind");
        double windSpeed = wind != null ? wind.optDouble("speed", 0) : 0;

        return new WeatherInfo(
                cityName,
                temperature,
                capitalizeDescription(description),
                humidity,
                windSpeed
        );
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
