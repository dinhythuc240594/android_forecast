# Forecast Weather

Ứng dụng Android xem thời tiết theo vị trí hiện tại hoặc theo danh sách thành phố, dữ liệu lấy từ [OpenWeatherMap](https://openweathermap.org/api).

## Tính năng

- **Màn hình chính (`MainActivity`)**: Lấy vị trí (Google Play Services — Fused Location), gọi API và hiển thị nhiệt độ, mô tả, độ ẩm, gió. Có làm mới và dự phòng **Hà Nội** khi không có quyền vị trí, GPS tắt, hoặc hết thời gian chờ định vị.
- **Danh sách thành phố (`CityListActivity`)**: Chọn một trong các địa điểm (chủ yếu Việt Nam) để xem chi tiết.
- **Chi tiết (`DetailActivity`)**: Thời tiết theo tên thành phố nhận từ danh sách.
- **Cài đặt (`SettingsActivity`)**: Đơn vị nhiệt độ (°C / °F) và ngôn ngữ giao diện (Tiếng Việt / English). API trả mô tả thời tiết theo ngôn ngữ đã chọn.

## Yêu cầu môi trường

- **Android Studio** (khuyến nghị bản hỗ trợ AGP 9 / Gradle 9)
- **JDK 11**
- Thiết bị hoặc emulator **API 24+** (minSdk 24), target/compile SDK **36**

## Cấu hình API

1. Tạo tài khoản và lấy **API Key** tại [OpenWeatherMap](https://openweathermap.org/api) (endpoint dùng trong app: Current Weather `data/2.5/weather`).
2. Ở thư mục gốc project, tạo hoặc chỉnh file `local.properties` (file này thường đã được `.gitignore`, không commit key):

```properties
WEATHER_API_KEY=your_openweathermap_api_key_here
```

Key được đưa vào `BuildConfig.WEATHER_API_KEY` khi build. Nếu thiếu key, app vẫn chạy nhưng không tải được dữ liệu thời tiết và sẽ hiển thị hướng dẫn cấu hình.

## Build & chạy

```bash
# Windows
gradlew.bat assembleDebug

# macOS / Linux
./gradlew assembleDebug
```

Hoặc mở project trong Android Studio và chọn **Run** trên thiết bị/emulator đã bật vị trí nếu muốn thử luồng theo GPS.

## Quyền (permissions)

- `INTERNET` — gọi API thời tiết.
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — lấy vị trí cho màn hình chính (người dùng có thể từ chối; app sẽ dùng thành phố mặc định).

## Công nghệ chính

| Thành phần | Ghi chú |
|------------|---------|
| Ngôn ngữ | Java 11 |
| UI | AppCompat, Material, ConstraintLayout, Fragment |
| Mạng | Volley (`JsonObjectRequest`) |
| Vị trí | Google Play Services Location |
| Bản build | Android Gradle Plugin 9.x, Gradle Wrapper 9.1.x |

## Cấu trúc thư mục (rút gọn)

```
app/src/main/java/com/example/myapplication/
  MainActivity.java          # Launcher, thời tiết theo vị trí
  CityListActivity.java      # Danh sách thành phố
  DetailActivity.java        # Chi tiết theo tên thành phố
  SettingsActivity.java      # Đơn vị & ngôn ngữ
  WeatherRepository.java     # Gọi OpenWeatherMap
  WeatherInfo.java           # Model dữ liệu thời tiết
  WeatherPreferences.java    # Lưu đơn vị nhiệt độ
  LanguageHelper.java        # Ngôn ngữ ứng dụng & tham số `lang` cho API
app/src/main/res/
  layout/                    # XML màn hình
  values/, values-vi/, values-en/  # Chuỗi đa ngôn ngữ
```

## Kiểm thử

- Unit test: `app/src/test/`
- Instrumented test: `app/src/androidTest/`

## Giấy phép

Thêm file LICENSE nếu bạn muốn công khai điều khoản sử dụng mã nguồn.
