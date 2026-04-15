package com.example.myapplication;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private static final int NOTIF_PERMISSION_REQUEST = 200;

    private RadioButton rbCelsius;
    private RadioButton rbFahrenheit;
    private RadioButton rbVietnamese;
    private RadioButton rbEnglish;
    private MaterialButton btnSaveSettings;

    private SwitchMaterial switchNotification;
    private View layoutNotifTime;
    private TextView txtNotifTime;
    private MaterialButton btnPickTime;
    private int pendingNotifHour;
    private int pendingNotifMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LanguageHelper.applySavedLanguage(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> getOnBackPressedDispatcher().onBackPressed());

        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        rbVietnamese = findViewById(R.id.rbVietnamese);
        rbEnglish = findViewById(R.id.rbEnglish);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        switchNotification = findViewById(R.id.switchNotification);
        layoutNotifTime = findViewById(R.id.layoutNotifTime);
        txtNotifTime = findViewById(R.id.txtNotifTime);
        btnPickTime = findViewById(R.id.btnPickTime);

        String currentUnit = WeatherPreferences.getUnit(this);
        if ("metric".equals(currentUnit)) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        String currentLanguage = LanguageHelper.getCurrentLanguage(this);
        if (LanguageHelper.LANGUAGE_VIETNAMESE.equals(currentLanguage)) {
            rbVietnamese.setChecked(true);
        } else {
            rbEnglish.setChecked(true);
        }

        setupNotificationControls();

        btnSaveSettings.setOnClickListener(v -> saveAllSettings());
    }

    private void setupNotificationControls() {
        boolean enabled = WeatherPreferences.isDailyNotifEnabled(this);
        pendingNotifHour = WeatherPreferences.getDailyNotifHour(this);
        pendingNotifMinute = WeatherPreferences.getDailyNotifMinute(this);

        switchNotification.setChecked(enabled);
        updateNotifTimeUi();
        layoutNotifTime.setAlpha(enabled ? 1f : 0.4f);
        btnPickTime.setEnabled(enabled);

        switchNotification.setOnCheckedChangeListener((btn, checked) -> {
            layoutNotifTime.setAlpha(checked ? 1f : 0.4f);
            btnPickTime.setEnabled(checked);
        });

        btnPickTime.setOnClickListener(v -> showTimePicker());
    }

    private void showTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            pendingNotifHour = hourOfDay;
            pendingNotifMinute = minute;
            updateNotifTimeUi();
        }, pendingNotifHour, pendingNotifMinute, true).show();
    }

    private void updateNotifTimeUi() {
        String timeStr = String.format(Locale.getDefault(), "%02d:%02d", pendingNotifHour, pendingNotifMinute);
        btnPickTime.setText(timeStr);
        txtNotifTime.setText(getString(R.string.settings_notif_time_fmt, timeStr));
    }

    private void saveAllSettings() {
        String selectedUnit = rbFahrenheit.isChecked() ? "imperial" : "metric";
        String selectedLanguage = rbVietnamese.isChecked()
                ? LanguageHelper.LANGUAGE_VIETNAMESE
                : LanguageHelper.LANGUAGE_ENGLISH;

        WeatherPreferences.saveUnit(this, selectedUnit);
        WeatherPreferences.saveLanguage(this, selectedLanguage);
        LanguageHelper.applyLanguage(selectedLanguage);

        boolean wantNotif = switchNotification.isChecked();
        boolean wasEnabled = WeatherPreferences.isDailyNotifEnabled(this);

        if (wantNotif) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                    && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIF_PERMISSION_REQUEST);
                return;
            }
            applyNotificationSchedule(true);
        } else {
            if (wasEnabled) {
                applyNotificationSchedule(false);
            }
        }

        Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void applyNotificationSchedule(boolean enable) {
        WeatherPreferences.setDailyNotifEnabled(this, enable);
        if (enable) {
            WeatherPreferences.setDailyNotifTime(this, pendingNotifHour, pendingNotifMinute);
            NotificationScheduler.schedule(this);
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", pendingNotifHour, pendingNotifMinute);
            Toast.makeText(this, getString(R.string.settings_notif_scheduled, timeStr), Toast.LENGTH_SHORT).show();
        } else {
            NotificationScheduler.cancel(this);
            Toast.makeText(this, R.string.settings_notif_cancelled, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIF_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                applyNotificationSchedule(true);
                Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                switchNotification.setChecked(false);
                Toast.makeText(this, R.string.settings_notif_permission_needed, Toast.LENGTH_LONG).show();
            }
        }
    }
}
