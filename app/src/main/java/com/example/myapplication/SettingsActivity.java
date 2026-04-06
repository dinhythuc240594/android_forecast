package com.example.myapplication;

import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private RadioButton rbCelsius;
    private RadioButton rbFahrenheit;
    private RadioButton rbVietnamese;
    private RadioButton rbEnglish;
    private MaterialButton btnSaveSettings;

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

        btnSaveSettings.setOnClickListener(v -> {
            String selectedUnit = rbFahrenheit.isChecked() ? "imperial" : "metric";
            String selectedLanguage = rbVietnamese.isChecked()
                    ? LanguageHelper.LANGUAGE_VIETNAMESE
                    : LanguageHelper.LANGUAGE_ENGLISH;

            WeatherPreferences.saveUnit(this, selectedUnit);
            WeatherPreferences.saveLanguage(this, selectedLanguage);
            LanguageHelper.applyLanguage(selectedLanguage);

            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
