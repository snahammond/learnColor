package com.example.learncolor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class LearningItemCardView extends FrameLayout {
    private final int cornerRadius;

    public LearningItemCardView(Context context, int cornerRadius) {
        super(context);
        this.cornerRadius = cornerRadius;
        setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setPadding(0, 0, 0, 0);
        setClipToOutline(true);
    }

    public void bind(LearningItem item, boolean selected) {
        removeAllViews();

        GradientDrawable cardBackground = new GradientDrawable();
        cardBackground.setShape(GradientDrawable.RECTANGLE);
        cardBackground.setColor(Color.WHITE);
        cardBackground.setCornerRadius(cornerRadius);
        cardBackground.setStroke(
                selected ? dpToPx(2) : dpToPx(1),
                selected ? Color.parseColor("#7C6BFF") : Color.argb(110, 148, 163, 184)
        );
        setBackground(cardBackground);

        FrameLayout previewHolder = new FrameLayout(getContext());
        FrameLayout.LayoutParams previewParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        );
        previewHolder.setLayoutParams(previewParams);

        if (item.hasDrawableResource()) {
            ImageView resourceImage = new ImageView(getContext());
            FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            );
            resourceImage.setLayoutParams(imageParams);
            resourceImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            resourceImage.setImageResource(item.getDrawableResId());
            previewHolder.addView(resourceImage);
            addView(previewHolder);
            setElevation(selected ? 8f : 3f);
            setTranslationZ(selected ? 2f : 0f);
            return;
        }

        switch (item.getVisualType()) {
            case COLOR:
                View colorView = new View(getContext());
                FrameLayout.LayoutParams colorParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                colorView.setLayoutParams(colorParams);
                colorView.setBackgroundColor(item.getColor());
                previewHolder.addView(colorView);
                break;

            case NUMBER:
                TextView numberText = new TextView(getContext());
                FrameLayout.LayoutParams numberParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                numberText.setLayoutParams(numberParams);
                numberText.setGravity(Gravity.CENTER);
                numberText.setText(item.getGeneratedText());
                numberText.setTextColor(item.getColor());
                numberText.setTextSize(24f);
                numberText.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                numberText.setShadowLayer(8f, 0f, 0f, Color.WHITE);
                previewHolder.addView(numberText);
                break;

            case SHAPE:
                ShapePreviewView shapePreview = new ShapePreviewView(getContext());
                FrameLayout.LayoutParams shapeParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                shapePreview.setLayoutParams(shapeParams);
                shapePreview.setShapeName(item.getName(), item.getColor());
                previewHolder.addView(shapePreview);
                break;

            case IMAGE:
                ImageView imageView = new ImageView(getContext());
                FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                imageView.setLayoutParams(imageParams);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                Bitmap thumbnail = item.getThumbnail();
                if (thumbnail != null) {
                    imageView.setImageBitmap(thumbnail);
                }
                previewHolder.addView(imageView);
                break;

            default:
                View defaultView = new View(getContext());
                defaultView.setBackgroundColor(item.getColor());
                previewHolder.addView(defaultView);
                break;
        }

        addView(previewHolder);
        setElevation(selected ? 8f : 3f);
        setTranslationZ(selected ? 2f : 0f);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
