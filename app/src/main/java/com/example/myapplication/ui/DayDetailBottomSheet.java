package com.example.myapplication.ui;

import android.app.Dialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.myapplication.R;
import com.example.myapplication.data.model.DailyForecast;
import com.example.myapplication.ui.widget.TemperatureChartView;
import com.example.myapplication.ui.widget.TemperatureChartView.ChartMetric;
import com.example.myapplication.util.LanguageHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

public class DayDetailBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_FORECAST = "arg_forecast";

    private DailyForecast forecast;
    private TemperatureChartView chart;
    private TextView txtRange;
    private TextView txtHint;
    private boolean isVi;

    public static DayDetailBottomSheet newInstance(DailyForecast forecast) {
        DayDetailBottomSheet sheet = new DayDetailBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_FORECAST, forecast);
        sheet.setArguments(args);
        return sheet;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bsd.findViewById(
                    com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_day_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            forecast = (DailyForecast) getArguments().getSerializable(ARG_FORECAST);
        }
        if (forecast == null) {
            dismiss();
            return;
        }

        isVi = LanguageHelper.LANGUAGE_VIETNAMESE.equals(
                LanguageHelper.getCurrentLanguage(requireContext()));

        TextView txtTitle = view.findViewById(R.id.txtDayTitle);
        txtRange = view.findViewById(R.id.txtTempRange);
        chart = view.findViewById(R.id.chartTemperature);
        txtHint = view.findViewById(R.id.txtChartHint);
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupMetrics);

        txtTitle.setText(forecast.getDayLabel());

        setupChips(view);

        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;
            int id = checkedIds.get(0);
            if (id == R.id.chipTemp) applyMetric(ChartMetric.TEMPERATURE);
            else if (id == R.id.chipRain) applyMetric(ChartMetric.RAIN);
            else if (id == R.id.chipHumidity) applyMetric(ChartMetric.HUMIDITY);
            else if (id == R.id.chipWind) applyMetric(ChartMetric.WIND);
        });

        applyMetric(ChartMetric.TEMPERATURE);
    }

    private void setupChips(View root) {
        int[][] states = {
                {android.R.attr.state_checked},
                {}
        };
        int[] bgColors = {0xE6FFFFFF, 0x55FFFFFF};
        ColorStateList chipBgCSL = new ColorStateList(states, bgColors);
        ColorStateList textCSL = ColorStateList.valueOf(0xDD000000);
        ColorStateList iconCSL = ColorStateList.valueOf(0xDD000000);

        int[] chipIds = {R.id.chipTemp, R.id.chipRain, R.id.chipHumidity, R.id.chipWind};
        String[] labelsVi = {"Nhiệt độ", "Mưa", "Độ ẩm", "Gió"};
        String[] labelsEn = {"Temp", "Rain", "Humidity", "Wind"};

        for (int i = 0; i < chipIds.length; i++) {
            Chip chip = root.findViewById(chipIds[i]);
            chip.setText(isVi ? labelsVi[i] : labelsEn[i]);
            chip.setChipBackgroundColor(chipBgCSL);
            chip.setTextColor(textCSL);
            chip.setChipIconTint(iconCSL);
            chip.setCheckedIconVisible(false);
        }
    }

    private void applyMetric(ChartMetric metric) {
        chart.setData(forecast.getSlots(), metric);

        switch (metric) {
            case TEMPERATURE:
                txtRange.setText(forecast.getTempMin() + "° – " + forecast.getTempMax() + "°");
                txtHint.setText(isVi ? "Dự báo mỗi 3 giờ" : "Forecast every 3 hours");
                break;
            case RAIN:
                txtRange.setText(isVi ? "Xác suất mưa" : "Rain probability");
                txtHint.setText(isVi ? "Dự báo mỗi 3 giờ" : "Forecast every 3 hours");
                break;
            case HUMIDITY:
                txtRange.setText(isVi ? "Độ ẩm tương đối" : "Relative humidity");
                txtHint.setText(isVi ? "Dự báo mỗi 3 giờ" : "Forecast every 3 hours");
                break;
            case WIND:
                txtRange.setText("m/s");
                txtHint.setText(isVi ? "Dự báo mỗi 3 giờ" : "Forecast every 3 hours");
                break;
        }
    }

    @Override
    public int getTheme() {
        return com.google.android.material.R.style.Theme_Design_BottomSheetDialog;
    }
}
