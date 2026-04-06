package com.example.myapplication;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class CityAdapter extends RecyclerView.Adapter<CityAdapter.CityViewHolder> {

    private final List<ProvinceItem> masterList = new ArrayList<>();
    private final List<ProvinceItem> cityList = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(ProvinceItem item);
    }

    public CityAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setMasterList(List<ProvinceItem> items) {
        masterList.clear();
        if (items != null) {
            masterList.addAll(items);
        }
        cityList.clear();
        cityList.addAll(masterList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        cityList.clear();
        if (query == null || query.trim().isEmpty()) {
            cityList.addAll(masterList);
        } else {
            String q = normalizeForSearch(query);
            for (ProvinceItem p : masterList) {
                if (normalizeForSearch(p.displayName).contains(q)) {
                    cityList.add(p);
                }
            }
        }
        notifyDataSetChanged();
    }

    /**
     * So khớp không phân biệt hoa thường và bỏ dấu (ví dụ "ha noi" khớp "Hà Nội").
     */
    private static String normalizeForSearch(String s) {
        if (s == null) {
            return "";
        }
        String n = Normalizer.normalize(s.trim(), Normalizer.Form.NFD);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n.length(); i++) {
            char c = n.charAt(i);
            if (Character.getType(c) != Character.NON_SPACING_MARK) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
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
        return cityList.size();
    }

    public static class CityViewHolder extends RecyclerView.ViewHolder {
        TextView txtCityName;

        public CityViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCityName = itemView.findViewById(R.id.txtCityName);
        }
    }
}
