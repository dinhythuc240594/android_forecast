package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder>{

    private List<ProvinceItem> cityList;
    private OnItemClickListener listener;

    // Interface để bắt sự kiện click và truyền ra ngoài Activity
    public interface OnItemClickListener {
        void onItemClick(ProvinceItem item);
    }

    public CityAdapter(List<ProvinceItem> cityList, OnItemClickListener listener) {
        this.cityList = cityList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        ProvinceItem item = cityList.get(position);
        holder.txtCityName.setText(item.displayName);
        holder.itemView.setContentDescription(
                holder.itemView.getContext().getString(R.string.city_list_item_cd_city, item.displayName));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cityList != null ? cityList.size() : 0;
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView txtCityName;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCityName = itemView.findViewById(R.id.txtCityName);
        }
    }


}
