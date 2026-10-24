package com.hammondtech.learn;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;

public class LearningItem {
    public enum VisualType {
        COLOR,
        NUMBER,
        SHAPE,
        IMAGE
    }

    private final String name;
    private final String label;
    private final VisualType visualType;
    private final int color;
    private final String generatedText;
    private final String imagePath;
    private final int drawableResId;
    private Bitmap thumbnail;

    public LearningItem(String name, String label, VisualType visualType, int color, String generatedText, String imagePath) {
        this(name, label, visualType, color, generatedText, imagePath, 0);
    }

    public LearningItem(String name, String label, VisualType visualType, int color, String generatedText, String imagePath, int drawableResId) {
        this.name = name;
        this.label = label;
        this.visualType = visualType;
        this.color = color;
        this.generatedText = generatedText;
        this.imagePath = imagePath;
        this.drawableResId = drawableResId;
    }

    public static LearningItem fromColor(String name, int color) {
        return new LearningItem(name, name, VisualType.COLOR, color, null, null, 0);
    }

    public static LearningItem fromColor(String name, int color, int drawableResId) {
        return new LearningItem(name, name, VisualType.COLOR, color, null, null, drawableResId);
    }

    public static LearningItem fromNumber(String number, int color) {
        return new LearningItem(number, number, VisualType.NUMBER, color, number, null, 0);
    }

    public static LearningItem fromNumber(String number, int color, int drawableResId) {
        return new LearningItem(number, number, VisualType.NUMBER, color, number, null, drawableResId);
    }

    public static LearningItem fromShape(String name, int color) {
        return new LearningItem(name, name, VisualType.SHAPE, color, name, null, 0);
    }

    public static LearningItem fromShape(String name, int color, int drawableResId) {
        return new LearningItem(name, name, VisualType.SHAPE, color, name, null, drawableResId);
    }

    public static LearningItem fromImage(String name, String imagePath, int tintColor) {
        return new LearningItem(name, name, VisualType.IMAGE, tintColor, null, imagePath, 0);
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label != null ? label : name;
    }

    public VisualType getVisualType() {
        return visualType;
    }

    public int getColor() {
        return color;
    }

    public String getGeneratedText() {
        return generatedText;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getDrawableResId() {
        return drawableResId;
    }

    public boolean hasDrawableResource() {
        return drawableResId != 0;
    }

    public boolean hasImage() {
        return imagePath != null && !imagePath.trim().isEmpty();
    }

    public Bitmap getThumbnail() {
        if (thumbnail != null) {
            return thumbnail;
        }
        thumbnail = createMiniatureSwatch(128, 128);
        return thumbnail;
    }

    public Bitmap createMiniatureSwatch(int width, int height) {
        if (hasImage()) {
            Bitmap source = BitmapFactory.decodeFile(imagePath);
            if (source != null) {
                Bitmap scaled = Bitmap.createScaledBitmap(source, width, height, true);
                if (scaled != null) {
                    return scaled;
                }
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setColor(color);
        canvas.drawRoundRect(
                0f,
                0f,
                (float) width,
                (float) height,
                24f,
                24f,
                backgroundPaint
        );

        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(Color.argb(120, 255, 255, 255));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4f);
        canvas.drawRoundRect(
                2f,
                2f,
                width - 2f,
                height - 2f,
                24f,
                24f,
                borderPaint
        );

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setFakeBoldText(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(height * 0.42f);

        if (visualType == VisualType.NUMBER && generatedText != null) {
            canvas.drawText(generatedText, width / 2f, height / 2f + (textPaint.getTextSize() * 0.34f), textPaint);
            return bitmap;
        }

        if (visualType == VisualType.SHAPE && generatedText != null) {
            drawShapeGlyph(canvas, width, height, generatedText, textPaint);
            return bitmap;
        }

        if (visualType == VisualType.IMAGE && hasImage()) {
            Paint overlayPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            overlayPaint.setColor(Color.argb(30, 255, 255, 255));
            canvas.drawRect(0f, 0f, width, height, overlayPaint);
        }

        return bitmap;
    }

    private void drawShapeGlyph(Canvas canvas, int width, int height, String shapeName, Paint paint) {
        float cx = width / 2f;
        float cy = height / 2f;
        float radius = Math.min(width, height) * 0.27f;
        Path path = new Path();

        switch (shapeName) {
            case "Circle":
                canvas.drawCircle(cx, cy, radius, paint);
                return;
            case "Triangle":
                path.moveTo(cx, cy - radius);
                path.lineTo(cx - radius, cy + radius);
                path.lineTo(cx + radius, cy + radius);
                path.close();
                canvas.drawPath(path, paint);
                return;
            case "Square":
                canvas.drawRect(cx - radius, cy - radius, cx + radius, cy + radius, paint);
                return;
            case "Rectangle":
                canvas.drawRect(cx - radius * 1.4f, cy - radius * 0.8f, cx + radius * 1.4f, cy + radius * 0.8f, paint);
                return;
            case "Pentagon":
                for (int i = 0; i < 5; i++) {
                    double angle = -Math.PI / 2 + i * (Math.PI * 2 / 5);
                    float x = (float) (cx + Math.cos(angle) * radius);
                    float y = (float) (cy + Math.sin(angle) * radius);
                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                path.close();
                canvas.drawPath(path, paint);
                return;
            case "Hexagon":
                for (int i = 0; i < 6; i++) {
                    double angle = -Math.PI / 2 + i * (Math.PI * 2 / 6);
                    float x = (float) (cx + Math.cos(angle) * radius);
                    float y = (float) (cy + Math.sin(angle) * radius);
                    if (i == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                path.close();
                canvas.drawPath(path, paint);
                return;
            case "Star":
                Path star = new Path();
                float outer = radius;
                float inner = radius * 0.45f;
                for (int i = 0; i < 10; i++) {
                    double angle = -Math.PI / 2 + i * (Math.PI / 5);
                    float radiusValue = (i % 2 == 0) ? outer : inner;
                    float x = (float) (cx + Math.cos(angle) * radiusValue);
                    float y = (float) (cy + Math.sin(angle) * radiusValue);
                    if (i == 0) {
                        star.moveTo(x, y);
                    } else {
                        star.lineTo(x, y);
                    }
                }
                star.close();
                canvas.drawPath(star, paint);
                return;
            default:
                canvas.drawCircle(cx, cy, radius, paint);
        }
    }
}
