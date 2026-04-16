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
import com.example.myapplication.data.model.DailyForecast;
import com.example.myapplication.util.WeatherIconUrl;

import java.util.List;
import java.util.Locale;

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.Holder> {

    public interface OnDayClickListener {
        void onDayClick(DailyForecast forecast);
    }

    private final List<DailyForecast> items;
    private OnDayClickListener listener;

    public DailyForecastAdapter(List<DailyForecast> items) {
        this.items = items;
    }

    public void setOnDayClickListener(OnDayClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_daily_forecast, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        DailyForecast d = items.get(position);
        holder.txtDay.setText(d.getDayLabel());
        holder.txtPop.setText(String.format(Locale.getDefault(), "%d%%", d.getPopPercent()));
        loadIcon(holder.imgDay, d.getIconDay());
        loadIcon(holder.imgNight, d.getIconNight());
        holder.txtTemps.setText(String.format(Locale.getDefault(), "%d° %d°", d.getTempMax(), d.getTempMin()));
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onDayClick(d);
        });
    }

    private static void loadIcon(ImageView target, String iconCode) {
        String url = WeatherIconUrl.forIcon(iconCode);
        if (url != null) {
            Glide.with(target.getContext()).load(url).into(target);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtDay;
        final TextView txtPop;
        final ImageView imgDay;
        final ImageView imgNight;
        final TextView txtTemps;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtDay = itemView.findViewById(R.id.txtDailyDay);
            txtPop = itemView.findViewById(R.id.txtDailyPop);
            imgDay = itemView.findViewById(R.id.imgDailyDay);
            imgNight = itemView.findViewById(R.id.imgDailyNight);
            txtTemps = itemView.findViewById(R.id.txtDailyTemps);
        }
    }
}
