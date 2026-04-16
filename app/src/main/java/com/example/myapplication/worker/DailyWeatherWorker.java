package com.example.myapplication.worker;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.myapplication.BuildConfig;
import com.example.myapplication.R;
import com.example.myapplication.data.local.AppDatabase;
import com.example.myapplication.data.local.CachedWeather;
import com.example.myapplication.data.local.CachedWeatherDao;
import com.example.myapplication.data.local.WeatherPreferences;
import com.example.myapplication.ui.activity.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class DailyWeatherWorker extends Worker {

    public static final String CHANNEL_ID = "daily_weather_channel";
    private static final int NOTIFICATION_ID = 7001;

    public DailyWeatherWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context ctx = getApplicationContext();
        createNotificationChannel(ctx);

        String apiKey = BuildConfig.WEATHER_API_KEY;
        if (apiKey == null || apiKey.isEmpty()) return Result.failure();

        CachedWeatherDao dao = AppDatabase.getInstance(ctx).cachedWeatherDao();
        String unit = WeatherPreferences.getUnit(ctx);

        CachedWeather cached = dao.get("current_location", unit);
        if (cached == null) {
            cached = dao.get("Hanoi", unit);
        }

        double lat = cached != null ? cached.latitude : 21.0285;
        double lon = cached != null ? cached.longitude : 105.8542;
        String cityName = cached != null ? cached.cityName : "Hanoi";

        try {
            String urlStr = String.format(Locale.US,
                    "https://api.openweathermap.org/data/3.0/onecall?lat=%f&lon=%f&units=%s&appid=%s&exclude=minutely,hourly,alerts",
                    lat, lon, unit, apiKey);

            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(15000);

            int code = conn.getResponseCode();
            if (code != 200) return Result.retry();

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();
            conn.disconnect();

            JSONObject json = new JSONObject(sb.toString());
            JSONObject current = json.getJSONObject("current");
            double temp = current.getDouble("temp");
            int humidity = current.optInt("humidity", 0);

            String description = "";
            JSONArray weatherArr = current.optJSONArray("weather");
            if (weatherArr != null && weatherArr.length() > 0) {
                description = weatherArr.getJSONObject(0).optString("description", "");
            }

            double tempMin = temp, tempMax = temp;
            JSONArray dailyArr = json.optJSONArray("daily");
            if (dailyArr != null && dailyArr.length() > 0) {
                JSONObject todayTemp = dailyArr.getJSONObject(0).optJSONObject("temp");
                if (todayTemp != null) {
                    tempMin = todayTemp.optDouble("min", temp);
                    tempMax = todayTemp.optDouble("max", temp);
                }
            }

            String unitSymbol = "imperial".equals(unit) ? "°F" : "°C";
            String title = String.format(Locale.getDefault(), "%s — %d%s",
                    cityName, Math.round(temp), unitSymbol);
            String body = String.format(Locale.getDefault(), "%s · %d%s / %d%s · %s %d%%",
                    capitalize(description),
                    Math.round(tempMin), unitSymbol,
                    Math.round(tempMax), unitSymbol,
                    ctx.getString(R.string.main_label_humidity), humidity);

            showNotification(ctx, title, body);
            return Result.success();

        } catch (Exception e) {
            return Result.retry();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase(Locale.getDefault()) + s.substring(1);
    }

    private void showNotification(Context ctx, String title, String body) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_location_city)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pi)
                .setAutoCancel(true);

        NotificationManagerCompat.from(ctx).notify(NOTIFICATION_ID, builder.build());
    }

    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.notif_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription(context.getString(R.string.notif_channel_desc));
            NotificationManager mgr = context.getSystemService(NotificationManager.class);
            if (mgr != null) mgr.createNotificationChannel(channel);
        }
    }
}
