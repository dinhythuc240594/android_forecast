package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroupUnit;
    private RadioButton rbCelsius, rbFahrenheit;
    private Button btnSaveSettings;

    // Tên file lưu trữ cấu hình
    private static final String PREF_NAME = "WeatherAppPrefs";
    private static final String KEY_UNIT = "unit"; // Giá trị lưu sẽ là "metric" hoặc "imperial"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ View
        radioGroupUnit = findViewById(R.id.radioGroupUnit);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        btnSaveSettings = findViewById(R.id.btnSaveSettings);

        // Khởi tạo SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        // Đọc cấu hình cũ để check vào RadioButton tương ứng (Mặc định là "metric" - Độ C)
        String currentUnit = sharedPreferences.getString(KEY_UNIT, "metric");
        if (currentUnit.equals("metric")) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        // Xử lý sự kiện lưu
        btnSaveSettings.setOnClickListener(v -> {
            String selectedUnit = "metric"; // Mặc định

            if (rbFahrenheit.isChecked()) {
                selectedUnit = "imperial";
            }

            // Lưu vào SharedPreferences
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_UNIT, selectedUnit);
            editor.apply(); // apply() lưu bất đồng bộ, tốt hơn commit()

            Toast.makeText(SettingsActivity.this, "Đã lưu cài đặt!", Toast.LENGTH_SHORT).show();

            // Đóng màn hình cài đặt để quay về màn hình trước đó
            finish();
        });
    }
}
