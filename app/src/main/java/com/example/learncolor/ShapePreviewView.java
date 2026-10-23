package com.example.learncolor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ShapePreviewView extends View {
    private String shapeName = "Circle";
    private int shapeColor = Color.parseColor("#FF7A59");
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public ShapePreviewView(Context context) {
        this(context, null);
    }

    public ShapePreviewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(12f);
        outlinePaint.setColor(Color.parseColor("#111827"));
        outlinePaint.setStrokeJoin(Paint.Join.ROUND);
        outlinePaint.setStrokeCap(Paint.Cap.ROUND);

        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(shapeColor);
    }

    public void setShapeName(String shapeName, int shapeColor) {
        this.shapeName = shapeName == null ? "Circle" : shapeName;
        this.shapeColor = shapeColor;
        fillPaint.setColor(shapeColor);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float size = Math.min(getWidth(), getHeight()) * 0.62f;

        switch (shapeName) {
            case "Circle":
                canvas.drawCircle(centerX, centerY, size / 2.2f, fillPaint);
                canvas.drawCircle(centerX, centerY, size / 2.2f, outlinePaint);
                break;
            case "Triangle":
                drawPolygon(canvas, centerX, centerY, size / 1.8f, 3, -Math.PI / 2);
                break;
            case "Square":
                drawRectShape(canvas, centerX, centerY, size / 1.8f, size / 1.8f);
                break;
            case "Rectangle":
                drawRectShape(canvas, centerX, centerY, size / 1.55f, size / 2.2f);
                break;
            case "Pentagon":
                drawPentagon(canvas, centerX, centerY, size / 1.8f);
                break;
            case "Hexagon":
                drawPolygon(canvas, centerX, centerY, size / 1.8f, 6, -Math.PI / 2);
                break;
            case "Star":
                drawStar(canvas, centerX, centerY, size / 1.9f, size / 3.5f);
                break;
            default:
                canvas.drawCircle(centerX, centerY, size / 2.2f, fillPaint);
                canvas.drawCircle(centerX, centerY, size / 2.2f, outlinePaint);
                break;
        }
    }

    private void drawRectShape(Canvas canvas, float cx, float cy, float width, float height) {
        RectF rect = new RectF(cx - width / 2f, cy - height / 2f, cx + width / 2f, cy + height / 2f);
        canvas.drawRect(rect, fillPaint);
        canvas.drawRect(rect, outlinePaint);
    }

    private void drawPolygon(Canvas canvas, float cx, float cy, float radius, int sides, double startAngle) {
        Path path = new Path();
        for (int i = 0; i < sides; i++) {
            double angle = startAngle + (Math.PI * 2 * i / sides);
            float x = (float) (cx + Math.cos(angle) * radius);
            float y = (float) (cy + Math.sin(angle) * radius);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, fillPaint);
        canvas.drawPath(path, outlinePaint);
    }

    private void drawPentagon(Canvas canvas, float cx, float cy, float radius) {
        Path path = new Path();
        double baseAngle = -Math.PI / 2 + (Math.PI / 5);
        for (int i = 0; i < 5; i++) {
            double angle = baseAngle + (Math.PI * 2 * i / 5);
            float x = (float) (cx + Math.cos(angle) * radius);
            float y = (float) (cy + Math.sin(angle) * radius);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, fillPaint);
        canvas.drawPath(path, outlinePaint);
    }

    private void drawStar(Canvas canvas, float cx, float cy, float outerRadius, float innerRadius) {
        Path path = new Path();
        int points = 10;
        for (int i = 0; i < points; i++) {
            double angle = -Math.PI / 2 + (Math.PI * i / (points / 2));
            float radius = (i % 2 == 0) ? outerRadius : innerRadius;
            float x = (float) (cx + Math.cos(angle) * radius);
            float y = (float) (cy + Math.sin(angle) * radius);
            if (i == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }
        path.close();
        canvas.drawPath(path, fillPaint);
        canvas.drawPath(path, outlinePaint);
    }
}
