package com.example.learncolor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ColorWheelView extends View {
    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    private int selectedColor = Color.RED;
    private float selectedAngle = 0f;
    private float centerX;
    private float centerY;
    private float radius;
    private final RectF wheelBounds = new RectF();
    private final Paint wheelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private OnColorSelectedListener onColorSelectedListener;

    public ColorWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setClickable(true);
        markerPaint.setStyle(Paint.Style.STROKE);
        markerPaint.setStrokeWidth(14f);
        markerPaint.setColor(Color.WHITE);
        updateSelectedAngleFromColor();
    }

    public int getSelectedColor() {
        return selectedColor;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
        updateSelectedAngleFromColor();
        invalidate();
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.onColorSelectedListener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(w, h) / 2f - 36f;
        wheelBounds.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        updateSelectedAngleFromColor();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        SweepGradient hueGradient = new SweepGradient(centerX, centerY, buildHuePalette(), null);
        wheelPaint.setShader(hueGradient);
        canvas.drawOval(wheelBounds, wheelPaint);

        centerPaint.setColor(selectedColor);
        canvas.drawCircle(centerX, centerY, radius * 0.36f, centerPaint);

        double radians = Math.toRadians(selectedAngle);
        float markerX = (float) (centerX + Math.cos(radians) * radius * 0.85f);
        float markerY = (float) (centerY + Math.sin(radians) * radius * 0.85f);
        canvas.drawCircle(markerX, markerY, 20f, markerPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - centerX;
                float dy = event.getY() - centerY;
                double distance = Math.hypot(dx, dy);

                if (distance <= radius + 10) {
                    double angle = ((Math.toDegrees(Math.atan2(dy, dx)) + 360.0) % 360.0);
                    selectedAngle = (float) angle;
                    selectedColor = Color.HSVToColor(new float[]{selectedAngle, 1f, 1f});
                    if (onColorSelectedListener != null) {
                        onColorSelectedListener.onColorSelected(selectedColor);
                    }
                    invalidate();
                    return true;
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private int[] buildHuePalette() {
        int[] palette = new int[361];
        for (int i = 0; i < 361; i++) {
            palette[i] = Color.HSVToColor(new float[]{i, 1f, 1f});
        }
        return palette;
    }

    private void updateSelectedAngleFromColor() {
        float[] hsv = new float[3];
        Color.colorToHSV(selectedColor, hsv);
        selectedAngle = hsv[0];
    }
}
