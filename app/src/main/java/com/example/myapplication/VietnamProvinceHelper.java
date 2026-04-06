package com.example.myapplication;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Chuẩn hoá tên từ provinces.open-api.vn (v2) để gọi OpenWeather {@code q}.
 * OpenWeather chủ yếu nhận tên tiếng Anh + {@code ,VN}, không ổn định với chuỗi tiếng Việt có dấu.
 */
public final class VietnamProvinceHelper {

    private static final Pattern COMBINING_MARKS = Pattern.compile("\\p{M}+");

    /**
     * {@code codename} từ API (ổn định) → tham số {@code q} cho OpenWeather.
     */
    private static final Map<String, String> CODE_TO_OPEN_WEATHER_Q = new HashMap<>();

    static {
        CODE_TO_OPEN_WEATHER_Q.put("ha_noi", "Hanoi,VN");
        CODE_TO_OPEN_WEATHER_Q.put("cao_bang", "Cao Bang,VN");
        CODE_TO_OPEN_WEATHER_Q.put("tuyen_quang", "Tuyen Quang,VN");
        CODE_TO_OPEN_WEATHER_Q.put("dien_bien", "Dien Bien Phu,VN");
        CODE_TO_OPEN_WEATHER_Q.put("lai_chau", "Lai Chau,VN");
        CODE_TO_OPEN_WEATHER_Q.put("son_la", "Son La,VN");
        CODE_TO_OPEN_WEATHER_Q.put("lao_cai", "Lao Cai,VN");
        CODE_TO_OPEN_WEATHER_Q.put("thai_nguyen", "Thai Nguyen,VN");
        CODE_TO_OPEN_WEATHER_Q.put("lang_son", "Lang Son,VN");
        CODE_TO_OPEN_WEATHER_Q.put("quang_ninh", "Ha Long,VN");
        CODE_TO_OPEN_WEATHER_Q.put("bac_ninh", "Bac Ninh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("phu_tho", "Viet Tri,VN");
        /* OpenWeather thường khớp "Haiphong" (một từ) hơn "Hai Phong" */
        CODE_TO_OPEN_WEATHER_Q.put("hai_phong", "Haiphong,VN");
        CODE_TO_OPEN_WEATHER_Q.put("hung_yen", "Hung Yen,VN");
        CODE_TO_OPEN_WEATHER_Q.put("ninh_binh", "Ninh Binh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("thanh_hoa", "Thanh Hoa,VN");
        CODE_TO_OPEN_WEATHER_Q.put("nghe_an", "Vinh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("ha_tinh", "Ha Tinh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("quang_tri", "Dong Ha,VN");
        CODE_TO_OPEN_WEATHER_Q.put("hue", "Hue,VN");
        CODE_TO_OPEN_WEATHER_Q.put("da_nang", "Da Nang,VN");
        CODE_TO_OPEN_WEATHER_Q.put("quang_ngai", "Quang Ngai,VN");
        CODE_TO_OPEN_WEATHER_Q.put("gia_lai", "Pleiku,VN");
        CODE_TO_OPEN_WEATHER_Q.put("khanh_hoa", "Nha Trang,VN");
        CODE_TO_OPEN_WEATHER_Q.put("dak_lak", "Buon Ma Thuot,VN");
        CODE_TO_OPEN_WEATHER_Q.put("lam_dong", "Da Lat,VN");
        CODE_TO_OPEN_WEATHER_Q.put("dong_nai", "Bien Hoa,VN");
        CODE_TO_OPEN_WEATHER_Q.put("ho_chi_minh", "Ho Chi Minh City,VN");
        CODE_TO_OPEN_WEATHER_Q.put("tay_ninh", "Tay Ninh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("dong_thap", "Cao Lanh,VN");
        CODE_TO_OPEN_WEATHER_Q.put("vinh_long", "Vinh Long,VN");
        CODE_TO_OPEN_WEATHER_Q.put("an_giang", "Long Xuyen,VN");
        CODE_TO_OPEN_WEATHER_Q.put("can_tho", "Can Tho,VN");
        CODE_TO_OPEN_WEATHER_Q.put("ca_mau", "Ca Mau,VN");
    }

    private VietnamProvinceHelper() {
    }

    /**
     * Dùng {@code codename} từ API để lấy chuỗi OpenWeather; fallback nếu thiếu map.
     */
    public static String toOpenWeatherQuery(String administrativeName, String codename) {
        if (codename != null) {
            String key = codename.trim().toLowerCase();
            String mapped = CODE_TO_OPEN_WEATHER_Q.get(key);
            if (mapped != null) {
                return mapped;
            }
        }
        return toOpenWeatherQueryFallback(administrativeName);
    }

    /**
     * Bỏ tiền tố "Thành phố " / "Tỉnh ", bỏ dấu tiếng Việt rồi thêm ",VN".
     * OpenWeather không ổn định với chuỗi có dấu trong {@code q}.
     */
    private static String toOpenWeatherQueryFallback(String administrativeName) {
        if (administrativeName == null) {
            return "";
        }
        String s = administrativeName.trim();
        if (s.startsWith("Thành phố ")) {
            s = s.substring("Thành phố ".length());
        } else if (s.startsWith("Tỉnh ")) {
            s = s.substring("Tỉnh ".length());
        }
        s = stripVietnameseDiacritics(s);
        s = s.trim();
        if (s.isEmpty()) {
            return "";
        }
        return s + ",VN";
    }

    private static String stripVietnameseDiacritics(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        s = s.replace('đ', 'd').replace('Đ', 'D');
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return COMBINING_MARKS.matcher(n).replaceAll("").trim();
    }
}
