package com.example.myapplication.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.example.myapplication.data.model.DaySlot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TemperatureChartView extends View {

    public enum ChartMetric {
        TEMPERATURE(0xFFFFFFFF, 0xAA7DD3FC, 0x30FFFFFF, "°"),
        RAIN(0xFF7DD3FC, 0xFF3B82F6, 0x307DD3FC, "%"),
        HUMIDITY(0xFF5EEAD4, 0xFF0D9488, 0x305EEAD4, "%"),
        WIND(0xFF86EFAC, 0xFF22C55E, 0x3086EFAC, "m/s"),
        AQI(0xFFFBBF24, 0xFFF59E0B, 0x30FBBF24, ""),
        UV(0xFFC084FC, 0xFF9333EA, 0x30C084FC, "");

        public final int lineColorTop;
        public final int lineColorBottom;
        public final int fillColor;
        public final String unit;

        ChartMetric(int lineColorTop, int lineColorBottom, int fillColor, String unit) {
            this.lineColorTop = lineColorTop;
            this.lineColorBottom = lineColorBottom;
            this.fillColor = fillColor;
            this.unit = unit;
        }
    }

    private List<DaySlot> slots = new ArrayList<>();
    private ChartMetric metric = ChartMetric.TEMPERATURE;

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint dotStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint valuePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint timeTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint gridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint noDataPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path linePath = new Path();
    private final Path fillPath = new Path();

    public TemperatureChartView(Context context) {
        super(context);
        init();
    }

    public TemperatureChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TemperatureChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(dp(2.5f));
        linePaint.setStrokeCap(Paint.Cap.ROUND);
        linePaint.setStrokeJoin(Paint.Join.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);

        dotPaint.setStyle(Paint.Style.FILL);
        dotPaint.setColor(Color.WHITE);

        dotStrokePaint.setStyle(Paint.Style.STROKE);
        dotStrokePaint.setStrokeWidth(dp(2f));
        dotStrokePaint.setColor(0xFF1D4ED8);

        valuePaint.setTextAlign(Paint.Align.CENTER);
        valuePaint.setColor(Color.WHITE);
        valuePaint.setTextSize(sp(12));
        valuePaint.setFakeBoldText(true);

        timeTextPaint.setTextAlign(Paint.Align.CENTER);
        timeTextPaint.setColor(0xAAFFFFFF);
        timeTextPaint.setTextSize(sp(11));

        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setStrokeWidth(dp(0.5f));
        gridPaint.setColor(0x22FFFFFF);
        gridPaint.setPathEffect(new DashPathEffect(new float[]{dp(4), dp(4)}, 0));

        noDataPaint.setTextAlign(Paint.Align.CENTER);
        noDataPaint.setColor(0x88FFFFFF);
        noDataPaint.setTextSize(sp(14));
    }

    public void setData(List<DaySlot> slots, ChartMetric metric) {
        this.slots = slots != null ? slots : new ArrayList<>();
        this.metric = metric;
        invalidate();
    }

    private double extractValue(DaySlot slot) {
        switch (metric) {
            case RAIN:
                return slot.getPopPercent();
            case HUMIDITY:
                return slot.getHumidity();
            case WIND:
                return slot.getWindSpeed();
            default:
                return slot.getTemp();
        }
    }

    private String formatValue(double value) {
        switch (metric) {
            case TEMPERATURE:
                return Math.round(value) + "°";
            case RAIN:
            case HUMIDITY:
                return Math.round(value) + "%";
            case WIND:
                return String.format(Locale.getDefault(), "%.1f", value);
            default:
                return String.valueOf(Math.round(value));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (metric == ChartMetric.AQI || metric == ChartMetric.UV) {
            drawNoData(canvas);
            return;
        }

        if (slots.size() < 2) {
            if (slots.size() == 1) drawSinglePoint(canvas);
            return;
        }

        float w = getWidth();
        float h = getHeight();
        float padLeft = dp(24);
        float padRight = dp(24);
        float padTop = dp(32);
        float padBottom = dp(28);
        float chartLeft = padLeft;
        float chartRight = w - padRight;
        float chartTop = padTop;
        float chartBottom = h - padBottom;

        double minVal = Double.MAX_VALUE, maxVal = -Double.MAX_VALUE;
        for (DaySlot s : slots) {
            double v = extractValue(s);
            minVal = Math.min(minVal, v);
            maxVal = Math.max(maxVal, v);
        }
        double range = maxVal - minVal;
        if (range < 1) {
            maxVal += 0.5;
            minVal -= 0.5;
            range = 1;
        }

        int n = slots.size();
        float spacing = (chartRight - chartLeft) / (n - 1);

        float[] xs = new float[n];
        float[] ys = new float[n];
        for (int i = 0; i < n; i++) {
            xs[i] = chartLeft + i * spacing;
            double v = extractValue(slots.get(i));
            float ratio = (float) ((v - minVal) / range);
            ys[i] = chartBottom - ratio * (chartBottom - chartTop);
        }

        for (int i = 0; i < n; i++) {
            canvas.drawLine(xs[i], chartTop - dp(4), xs[i], chartBottom + dp(4), gridPaint);
        }

        linePath.reset();
        fillPath.reset();
        linePath.moveTo(xs[0], ys[0]);
        fillPath.moveTo(xs[0], chartBottom);
        fillPath.lineTo(xs[0], ys[0]);

        for (int i = 1; i < n; i++) {
            float cx1 = (xs[i - 1] + xs[i]) / 2f;
            float cy1 = ys[i - 1];
            float cx2 = cx1;
            float cy2 = ys[i];
            linePath.cubicTo(cx1, cy1, cx2, cy2, xs[i], ys[i]);
            fillPath.cubicTo(cx1, cy1, cx2, cy2, xs[i], ys[i]);
        }
        fillPath.lineTo(xs[n - 1], chartBottom);
        fillPath.close();

        fillPaint.setShader(new LinearGradient(
                0, chartTop, 0, chartBottom,
                metric.fillColor, 0x05FFFFFF, Shader.TileMode.CLAMP));
        canvas.drawPath(fillPath, fillPaint);

        linePaint.setShader(new LinearGradient(
                0, chartTop, 0, chartBottom,
                metric.lineColorTop, metric.lineColorBottom, Shader.TileMode.CLAMP));
        canvas.drawPath(linePath, linePaint);
        linePaint.setShader(null);

        dotStrokePaint.setColor(metric.lineColorBottom);

        for (int i = 0; i < n; i++) {
            canvas.drawCircle(xs[i], ys[i], dp(4f), dotPaint);
            canvas.drawCircle(xs[i], ys[i], dp(4f), dotStrokePaint);

            String label = formatValue(extractValue(slots.get(i)));
            canvas.drawText(label, xs[i], ys[i] - dp(10), valuePaint);
            canvas.drawText(slots.get(i).getTimeLabel(), xs[i], chartBottom + dp(16), timeTextPaint);
        }
    }

    private void drawNoData(Canvas canvas) {
        canvas.drawText("—", getWidth() / 2f, getHeight() / 2f, noDataPaint);
    }

    private void drawSinglePoint(Canvas canvas) {
        DaySlot s = slots.get(0);
        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f - dp(10);
        dotStrokePaint.setColor(metric.lineColorBottom);
        canvas.drawCircle(cx, cy, dp(5), dotPaint);
        canvas.drawCircle(cx, cy, dp(5), dotStrokePaint);
        canvas.drawText(formatValue(extractValue(s)), cx, cy - dp(14), valuePaint);
        canvas.drawText(s.getTimeLabel(), cx, cy + dp(24), timeTextPaint);
    }

    private float dp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics());
    }

    private float sp(float value) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, getResources().getDisplayMetrics());
    }
}
