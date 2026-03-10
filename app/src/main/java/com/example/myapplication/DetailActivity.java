package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    private TextView txtDetailCity, txtDetailTemp, txtDetailDesc, txtHumidity, txtWind;
    private Button btnBack;

    // Nhớ dùng lại API Key của bạn nhé
    private final String API_KEY = "YOUR_API_KEY_HERE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Ánh xạ View
        txtDetailCity = findViewById(R.id.txtDetailCity);
        txtDetailTemp = findViewById(R.id.txtDetailTemp);
        txtDetailDesc = findViewById(R.id.txtDetailDesc);
        txtHumidity = findViewById(R.id.txtHumidity);
        txtWind = findViewById(R.id.txtWind);
        btnBack = findViewById(R.id.btnBack);

        // Nút quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // Nhận dữ liệu (Tên thành phố) từ Intent
        Intent intent = getIntent();
        String cityName = intent.getStringExtra("CITY_NAME");

        if (cityName != null && !cityName.isEmpty()) {
            txtDetailCity.setText(cityName);
            fetchWeatherDetails(cityName);
        } else {
            Toast.makeText(this, "Không nhận được tên thành phố", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchWeatherDetails(String cityName) {
        // Gọi API dựa trên tên thành phố (q=cityName)
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + cityName +
                "&appid=" + API_KEY + "&units=metric&lang=vi";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // 1. Lấy nhiệt độ và độ ẩm từ object "main"
                        JSONObject main = response.getJSONObject("main");
                        double temp = main.getDouble("temp");
                        int humidity = main.getInt("humidity");

                        // 2. Lấy mô tả thời tiết từ array "weather"
                        String description = response.getJSONArray("weather")
                                .getJSONObject(0).getString("description");

                        // 3. Lấy tốc độ gió từ object "wind"
                        JSONObject wind = response.getJSONObject("wind");
                        double windSpeed = wind.getDouble("speed");

                        // Cập nhật giao diện
                        txtDetailTemp.setText(Math.round(temp) + "°C");
                        txtDetailDesc.setText(description.substring(0, 1).toUpperCase() + description.substring(1));
                        txtHumidity.setText(humidity + "%");
                        txtWind.setText(windSpeed + " m/s");

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(DetailActivity.this, "Lỗi đọc dữ liệu thời tiết", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Toast.makeText(DetailActivity.this, "Không tìm thấy thông tin hoặc lỗi mạng", Toast.LENGTH_SHORT).show();
                });

        queue.add(jsonObjectRequest);
    }
}