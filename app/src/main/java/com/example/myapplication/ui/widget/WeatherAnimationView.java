package com.example.myapplication.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WeatherAnimationView extends View {

    public enum WeatherType {
        CLEAR, CLOUDS, RAIN, DRIZZLE, THUNDERSTORM, SNOW, MIST
    }

    private WeatherType weatherType = WeatherType.CLEAR;
    private boolean isNight;

    private final List<Particle> particles = new ArrayList<>();
    private final List<CloudShape> clouds = new ArrayList<>();
    private final List<LightningBolt> bolts = new ArrayList<>();
    private final Random random = new Random();
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();

    private long lastFrameTime;
    private boolean running;
    private int viewWidth;
    private int viewHeight;

    public WeatherAnimationView(Context context) {
        super(context);
        init();
    }

    public WeatherAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WeatherAnimationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setLayerType(LAYER_TYPE_HARDWARE, null);
    }

    public void setWeather(int weatherId, boolean night) {
        this.isNight = night;
        WeatherType newType = mapWeatherIdToType(weatherId);
        if (newType == this.weatherType && !particles.isEmpty()) return;
        this.weatherType = newType;
        resetParticles();
        if (weatherType == WeatherType.CLEAR && !isNight) {
            setVisibility(GONE);
        } else {
            setVisibility(VISIBLE);
        }
        invalidate();
    }

    private WeatherType mapWeatherIdToType(int id) {
        if (id >= 200 && id < 300) return WeatherType.THUNDERSTORM;
        if (id >= 300 && id < 400) return WeatherType.DRIZZLE;
        if (id >= 500 && id < 600) return WeatherType.RAIN;
        if (id >= 600 && id < 700) return WeatherType.SNOW;
        if (id >= 700 && id < 800) return WeatherType.MIST;
        if (id == 800) return WeatherType.CLEAR;
        if (id > 800 && id < 900) return WeatherType.CLOUDS;
        return WeatherType.CLEAR;
    }

    private void resetParticles() {
        particles.clear();
        clouds.clear();
        bolts.clear();
        if (viewWidth == 0 || viewHeight == 0) return;

        switch (weatherType) {
            case RAIN:
                createRainParticles(120);
                createClouds(4);
                break;
            case DRIZZLE:
                createRainParticles(50);
                createClouds(3);
                break;
            case THUNDERSTORM:
                createRainParticles(160);
                createClouds(5);
                break;
            case SNOW:
                createSnowParticles(80);
                createClouds(3);
                break;
            case MIST:
                createMistParticles(40);
                break;
            case CLOUDS:
                createClouds(5);
                break;
            case CLEAR:
                if (isNight) createStarParticles(60);
                break;
        }
    }

    private void createRainParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * viewWidth;
            p.y = random.nextFloat() * viewHeight;
            p.speedX = -1f + random.nextFloat() * -1f;
            p.speedY = 12f + random.nextFloat() * 8f;
            p.size = 1.5f + random.nextFloat() * 1.5f;
            p.length = 15f + random.nextFloat() * 25f;
            p.alpha = 80 + random.nextInt(120);
            particles.add(p);
        }
    }

    private void createSnowParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * viewWidth;
            p.y = random.nextFloat() * viewHeight;
            p.speedX = -0.5f + random.nextFloat() * 1.0f;
            p.speedY = 1.5f + random.nextFloat() * 2.5f;
            p.size = 3f + random.nextFloat() * 6f;
            p.alpha = 150 + random.nextInt(105);
            p.wobblePhase = random.nextFloat() * (float) (Math.PI * 2);
            particles.add(p);
        }
    }

    private void createMistParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * viewWidth;
            p.y = random.nextFloat() * viewHeight;
            p.speedX = 0.3f + random.nextFloat() * 0.6f;
            p.speedY = -0.1f + random.nextFloat() * 0.2f;
            p.size = 40f + random.nextFloat() * 60f;
            p.alpha = 30 + random.nextInt(50);
            particles.add(p);
        }
    }

    private void createStarParticles(int count) {
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            p.x = random.nextFloat() * viewWidth;
            p.y = random.nextFloat() * viewHeight * 0.7f;
            p.speedX = 0;
            p.speedY = 0;
            p.size = 1.5f + random.nextFloat() * 2.5f;
            p.alpha = 100 + random.nextInt(155);
            p.wobblePhase = random.nextFloat() * (float) (Math.PI * 2);
            p.wobbleSpeed = 1.5f + random.nextFloat() * 2f;
            particles.add(p);
        }
    }

    private void createClouds(int count) {
        for (int i = 0; i < count; i++) {
            CloudShape c = new CloudShape();
            c.x = random.nextFloat() * viewWidth * 1.5f - viewWidth * 0.25f;
            c.y = random.nextFloat() * viewHeight * 0.35f;
            c.width = 140f + random.nextFloat() * 180f;
            c.height = 40f + random.nextFloat() * 30f;
            c.speedX = 0.2f + random.nextFloat() * 0.5f;
            c.alpha = isNight ? (20 + random.nextInt(30)) : (30 + random.nextInt(50));
            clouds.add(c);
        }
    }

    public void startAnimation() {
        running = true;
        lastFrameTime = System.nanoTime();
        invalidate();
    }

    public void stopAnimation() {
        running = false;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        viewWidth = w;
        viewHeight = h;
        resetParticles();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (viewWidth == 0 || viewHeight == 0) return;

        long now = System.nanoTime();
        float dt = Math.min((now - lastFrameTime) / 1_000_000_000f, 0.05f);
        lastFrameTime = now;

        drawClouds(canvas, dt);

        switch (weatherType) {
            case RAIN:
            case DRIZZLE:
                drawRain(canvas, dt);
                break;
            case THUNDERSTORM:
                drawRain(canvas, dt);
                updateLightning(canvas, dt);
                break;
            case SNOW:
                drawSnow(canvas, dt);
                break;
            case MIST:
                drawMist(canvas, dt);
                break;
            case CLEAR:
                if (isNight) drawStars(canvas, dt);
                break;
            case CLOUDS:
                break;
        }

        if (running) {
            postInvalidateOnAnimation();
        }
    }

    private void drawRain(Canvas canvas, float dt) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (Particle p : particles) {
            p.x += p.speedX * dt * 60;
            p.y += p.speedY * dt * 60;
            if (p.y > viewHeight) {
                p.y = -p.length;
                p.x = random.nextFloat() * viewWidth;
            }
            if (p.x < -20) p.x = viewWidth + 10;

            paint.setStrokeWidth(p.size);
            paint.setColor(Color.argb(p.alpha, 180, 210, 255));
            canvas.drawLine(p.x, p.y, p.x + p.speedX * 0.4f, p.y + p.length, paint);
        }
    }

    private void drawSnow(Canvas canvas, float dt) {
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : particles) {
            p.wobblePhase += dt * 2f;
            p.x += (p.speedX + (float) Math.sin(p.wobblePhase) * 0.8f) * dt * 60;
            p.y += p.speedY * dt * 60;
            if (p.y > viewHeight + p.size) {
                p.y = -p.size * 2;
                p.x = random.nextFloat() * viewWidth;
            }
            if (p.x < -20) p.x = viewWidth + 10;
            if (p.x > viewWidth + 20) p.x = -10;

            int glow = (int) (p.alpha * 0.3f);
            paint.setColor(Color.argb(glow, 255, 255, 255));
            canvas.drawCircle(p.x, p.y, p.size * 1.8f, paint);

            paint.setColor(Color.argb(p.alpha, 255, 255, 255));
            canvas.drawCircle(p.x, p.y, p.size, paint);
        }
    }

    private void drawMist(Canvas canvas, float dt) {
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : particles) {
            p.x += p.speedX * dt * 60;
            p.y += p.speedY * dt * 60;
            if (p.x > viewWidth + p.size) p.x = -p.size;
            if (p.y < -p.size || p.y > viewHeight + p.size) {
                p.y = random.nextFloat() * viewHeight;
            }

            paint.setShader(new LinearGradient(
                    p.x - p.size, p.y,
                    p.x + p.size, p.y,
                    Color.argb(0, 200, 200, 200),
                    Color.argb(p.alpha, 200, 200, 200),
                    Shader.TileMode.CLAMP
            ));
            canvas.drawCircle(p.x, p.y, p.size, paint);
            paint.setShader(null);
        }
    }

    private void drawStars(Canvas canvas, float dt) {
        paint.setStyle(Paint.Style.FILL);
        for (Particle p : particles) {
            p.wobblePhase += dt * p.wobbleSpeed;
            float twinkle = 0.5f + 0.5f * (float) Math.sin(p.wobblePhase);
            int alpha = (int) (p.alpha * twinkle);

            paint.setColor(Color.argb((int) (alpha * 0.3f), 255, 255, 200));
            canvas.drawCircle(p.x, p.y, p.size * 2.5f, paint);

            paint.setColor(Color.argb(alpha, 255, 255, 230));
            canvas.drawCircle(p.x, p.y, p.size, paint);
        }
    }

    private void drawClouds(Canvas canvas, float dt) {
        paint.setStyle(Paint.Style.FILL);
        for (CloudShape c : clouds) {
            c.x += c.speedX * dt * 60;
            if (c.x > viewWidth + c.width) c.x = -c.width;

            int color = isNight ? Color.argb(c.alpha, 60, 65, 80) : Color.argb(c.alpha, 200, 210, 225);
            paint.setColor(color);

            float cx = c.x;
            float cy = c.y;
            canvas.drawOval(cx, cy, cx + c.width, cy + c.height, paint);
            canvas.drawOval(cx + c.width * 0.15f, cy - c.height * 0.4f,
                    cx + c.width * 0.6f, cy + c.height * 0.6f, paint);
            canvas.drawOval(cx + c.width * 0.4f, cy - c.height * 0.55f,
                    cx + c.width * 0.85f, cy + c.height * 0.5f, paint);
        }
    }

    private void updateLightning(Canvas canvas, float dt) {
        if (random.nextFloat() < dt * 0.4f) {
            LightningBolt bolt = new LightningBolt();
            bolt.x = viewWidth * 0.1f + random.nextFloat() * viewWidth * 0.8f;
            bolt.y = 0;
            bolt.ttl = 0.15f + random.nextFloat() * 0.1f;
            bolt.alpha = 200 + random.nextInt(55);
            bolts.add(bolt);
        }

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        for (int i = bolts.size() - 1; i >= 0; i--) {
            LightningBolt b = bolts.get(i);
            b.ttl -= dt;
            if (b.ttl <= 0) {
                bolts.remove(i);
                continue;
            }
            float fadeFactor = b.ttl / 0.25f;
            int a = (int) (b.alpha * Math.min(fadeFactor, 1f));
            paint.setStrokeWidth(3f);
            paint.setColor(Color.argb(a, 255, 255, 220));

            path.reset();
            float px = b.x;
            float py = b.y;
            path.moveTo(px, py);
            float endY = viewHeight * (0.4f + random.nextFloat() * 0.3f);
            int segments = 5 + random.nextInt(4);
            float segLen = endY / segments;
            for (int s = 0; s < segments; s++) {
                px += -15 + random.nextFloat() * 30;
                py += segLen;
                path.lineTo(px, py);
            }
            canvas.drawPath(path, paint);

            if (random.nextFloat() < 0.3f) {
                paint.setStrokeWidth(1.5f);
                paint.setColor(Color.argb(a / 2, 255, 255, 200));
                int branchSeg = 2 + random.nextInt(2);
                float bx = px;
                float by = py - segLen * 2;
                path.reset();
                path.moveTo(bx, by);
                for (int s = 0; s < branchSeg; s++) {
                    bx += -20 + random.nextFloat() * 40;
                    by += segLen * 0.6f;
                    path.lineTo(bx, by);
                }
                canvas.drawPath(path, paint);
            }
        }
    }

    private static class Particle {
        float x, y;
        float speedX, speedY;
        float size;
        float length;
        int alpha;
        float wobblePhase;
        float wobbleSpeed = 2f;
    }

    private static class CloudShape {
        float x, y;
        float width, height;
        float speedX;
        int alpha;
    }

    private static class LightningBolt {
        float x, y;
        float ttl;
        int alpha;
    }
}
