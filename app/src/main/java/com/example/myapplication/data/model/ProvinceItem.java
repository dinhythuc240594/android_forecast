package com.example.myapplication.data.model;

/**
 * Một tỉnh/thành từ API hành chính (depth=1): hiển thị tên tiếng Việt,
 * truy vấn thời tiết dùng chuỗi {@code shortName,VN}.
 */
public final class ProvinceItem {

    public final String displayName;
    public final String weatherQuery;

    public ProvinceItem(String displayName, String weatherQuery) {
        this.displayName = displayName;
        this.weatherQuery = weatherQuery;
    }
}
