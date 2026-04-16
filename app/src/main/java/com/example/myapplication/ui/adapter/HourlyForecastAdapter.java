package com.example.myapplication.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.data.model.HourlyForecast;
import com.example.myapplication.util.WeatherIconUrl;

import java.util.List;
import java.util.Locale;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.Holder> {

    private final List<HourlyForecast> items;

    public HourlyForecastAdapter(List<HourlyForecast> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_hourly_forecast, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        HourlyForecast h = items.get(position);
        holder.txtTime.setText(h.getTimeLabel());
        holder.txtTemp.setText(String.format(Locale.getDefault(), "%d°", Math.round(h.getTemp())));
        holder.txtPop.setText(String.format(Locale.getDefault(), "%d%%", h.getPopPercent()));
        String url = WeatherIconUrl.forIcon(h.getIconCode());
        if (url != null) {
            Glide.with(holder.imgWeather.getContext()).load(url).into(holder.imgWeather);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtTime;
        final ImageView imgWeather;
        final TextView txtTemp;
        final TextView txtPop;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtTime = itemView.findViewById(R.id.txtHourlyTime);
            imgWeather = itemView.findViewById(R.id.imgHourlyWeather);
            txtTemp = itemView.findViewById(R.id.txtHourlyTemp);
            txtPop = itemView.findViewById(R.id.txtHourlyPop);
        }
    }
}
