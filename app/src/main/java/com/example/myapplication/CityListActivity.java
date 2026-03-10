package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Arrays;
import java.util.List;

public class CityListActivity extends AppCompatActivity {

    private RecyclerView rvCities;
    private CityAdapter cityAdapter;
    private List<String> cityList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_list);

        rvCities = findViewById(R.id.rvCities);

        // Chuẩn bị dữ liệu: Danh sách một số tỉnh/thành Việt Nam (API OpenWeather nhận dạng tốt không dấu hoặc tiếng Anh, nhưng tiếng Việt có dấu vẫn dùng được tuỳ query)
        // Mẹo: Nếu API trả về lỗi với tên có dấu, bạn có thể truyền tên không dấu hoặc thêm chữ "Province" đằng sau.
        cityList = Arrays.asList(
                "Hanoi", "Ho Chi Minh City", "Da Nang", "Hai Phong", "Can Tho",
                "Hue", "Nha Trang", "Da Lat", "Vung Tau", "Ha Long",
                "Quy Nhon", "Vinh", "Nam Dinh", "Thanh Hoa", "Rach Gia"
        );

        // Cấu hình RecyclerView
        rvCities.setLayoutManager(new LinearLayoutManager(this));

        // Khởi tạo Adapter và xử lý sự kiện click
        cityAdapter = new CityAdapter(cityList, new CityAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(String city) {
                // Chuyển sang Màn hình 3 (DetailActivity) và gửi kèm tên thành phố
                Intent intent = new Intent(CityListActivity.this, DetailActivity.class);
                intent.putExtra("CITY_NAME", city);
                startActivity(intent);
            }
        });

        // Gắn Adapter vào RecyclerView
        rvCities.setAdapter(cityAdapter);
    }
}
