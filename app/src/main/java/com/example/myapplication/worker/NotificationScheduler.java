package com.example.myapplication.worker;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.myapplication.data.local.WeatherPreferences;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public final class NotificationScheduler {

    private static final String WORK_TAG = "daily_weather_notification";

    private NotificationScheduler() {}

    public static void schedule(Context context) {
        int hour = WeatherPreferences.getDailyNotifHour(context);
        int minute = WeatherPreferences.getDailyNotifMinute(context);

        long delayMs = computeInitialDelayMs(hour, minute);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                DailyWeatherWorker.class, 24, TimeUnit.HOURS)
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setConstraints(constraints)
                .addTag(WORK_TAG)
                .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_TAG,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
        );
    }

    public static void cancel(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_TAG);
    }

    static long computeInitialDelayMs(int targetHour, int targetMinute) {
        Calendar now = Calendar.getInstance();
        Calendar target = (Calendar) now.clone();
        target.set(Calendar.HOUR_OF_DAY, targetHour);
        target.set(Calendar.MINUTE, targetMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        if (target.before(now) || target.equals(now)) {
            target.add(Calendar.DAY_OF_MONTH, 1);
        }
        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}
