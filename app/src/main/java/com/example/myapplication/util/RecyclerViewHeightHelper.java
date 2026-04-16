package com.example.myapplication.util;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

/**
 * RecyclerView trong ScrollView/NestedScrollView với {@code layout_height="wrap_content"} thường
 * chỉ đo được một item; dùng helper này để gán chiều cao = tổng chiều cao các dòng (dự báo theo ngày).
 */
public final class RecyclerViewHeightHelper {

    private RecyclerViewHeightHelper() {
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void setVerticalListHeightToContent(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        if (adapter == null) {
            return;
        }
        int count = adapter.getItemCount();
        recyclerView.post(() -> {
            ViewGroup.LayoutParams lp = recyclerView.getLayoutParams();
            if (count == 0) {
                lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                recyclerView.setLayoutParams(lp);
                return;
            }
            int width = recyclerView.getWidth();
            if (width <= 0) {
                width = recyclerView.getContext().getResources().getDisplayMetrics().widthPixels;
            }
            int widthSpec = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
            int height = 0;
            for (int i = 0; i < count; i++) {
                int viewType = adapter.getItemViewType(i);
                RecyclerView.ViewHolder holder = adapter.createViewHolder(recyclerView, viewType);
                adapter.onBindViewHolder(holder, i);
                holder.itemView.measure(widthSpec, View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
                height += holder.itemView.getMeasuredHeight();
            }
            lp.height = height;
            recyclerView.setLayoutParams(lp);
        });
    }
}
