package com.example.learncolor;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.learncolor.databinding.ActivityMainBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private ActivityMainBinding binding;
    private TextToSpeech textToSpeech;

    private final List<ColorChoice> basicColors = new ArrayList<>(Arrays.asList(
            new ColorChoice("Red", Color.RED),
            new ColorChoice("Yellow", Color.YELLOW),
            new ColorChoice("Green", Color.GREEN),
            new ColorChoice("Blue", Color.BLUE)
    ));

    private final List<ColorChoice> advancedColors = new ArrayList<>(Arrays.asList(
            new ColorChoice("Red", Color.RED),
            new ColorChoice("Orange", Color.rgb(255, 153, 0)),
            new ColorChoice("Yellow", Color.YELLOW),
            new ColorChoice("Green", Color.GREEN),
            new ColorChoice("Blue", Color.BLUE),
            new ColorChoice("Purple", Color.rgb(128, 0, 255)),
            new ColorChoice("Pink", Color.rgb(255, 105, 180))
    ));

    private ColorMode currentMode = ColorMode.BASIC;
    private ColorChoice currentSelection = basicColors.get(0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textToSpeech = new TextToSpeech(this, this);

        binding.modeMenuButton.setOnClickListener(v -> toggleModeMenu());
        binding.basicModeButton.setOnClickListener(v -> {
            setMode(ColorMode.BASIC);
            binding.modeOptions.setVisibility(View.GONE);
        });
        binding.advancedModeButton.setOnClickListener(v -> {
            setMode(ColorMode.ADVANCED);
            binding.modeOptions.setVisibility(View.GONE);
        });
        binding.colorPreview.setOnClickListener(v -> speakCurrentColor());

        setMode(ColorMode.BASIC);
        binding.modeOptions.setVisibility(View.GONE);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
        }
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private void toggleModeMenu() {
        binding.modeOptions.setVisibility(
                binding.modeOptions.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
        );
        updateModeControls();
    }

    private void setMode(ColorMode mode) {
        currentMode = mode;

        List<ColorChoice> options = (mode == ColorMode.BASIC) ? basicColors : advancedColors;
        currentSelection = options.get(0);
        updateModeControls();
        updateColorStrip();
        updatePreview();
    }

    private void updateModeControls() {
        boolean basicSelected = currentMode == ColorMode.BASIC;

        binding.basicModeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        basicSelected ? Color.parseColor("#6200EE") : Color.parseColor("#E0E0E0")
                )
        );
        binding.advancedModeButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        basicSelected ? Color.parseColor("#E0E0E0") : Color.parseColor("#6200EE")
                )
        );

        binding.basicModeButton.setTextColor(basicSelected ? Color.WHITE : Color.BLACK);
        binding.advancedModeButton.setTextColor(basicSelected ? Color.BLACK : Color.WHITE);

        binding.modeMenuButton.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                        basicSelected ? Color.parseColor("#1A1A1A") : Color.parseColor("#6200EE")
                )
        );
    }

    private void updateColorStrip() {
        binding.colorBar.removeAllViews();
        List<ColorChoice> options = (currentMode == ColorMode.BASIC) ? basicColors : advancedColors;

        for (ColorChoice choice : options) {
            Button swatch = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    dpToPx(54), dpToPx(54)
            );
            params.setMargins(8, 0, 8, 0);
            swatch.setLayoutParams(params);
            swatch.setBackgroundColor(choice.getColor());
            swatch.setText("");
            swatch.setAlpha(choice.equals(currentSelection) ? 1.0f : 0.65f);
            swatch.setOnClickListener(v -> selectColor(choice));

            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.OVAL);
            border.setColor(choice.getColor());
            border.setStroke(4, choice.equals(currentSelection) ? Color.WHITE : Color.argb(80, 255, 255, 255));
            swatch.setBackground(border);

            binding.colorBar.addView(swatch);
        }
    }

    private void selectColor(ColorChoice choice) {
        currentSelection = choice;
        updateColorStrip();
        updatePreview();
    }

    private void updatePreview() {
        int color = currentSelection.getColor();

        GradientDrawable previewBackground = new GradientDrawable();
        previewBackground.setShape(GradientDrawable.RECTANGLE);
        previewBackground.setColor(color);
        previewBackground.setCornerRadius(dpToPx(32));
        previewBackground.setStroke(dpToPx(2), Color.argb(120, 255, 255, 255));
        binding.colorPreview.setBackground(previewBackground);

        GradientDrawable overlayBackground = new GradientDrawable();
        overlayBackground.setShape(GradientDrawable.RECTANGLE);
        overlayBackground.setCornerRadius(dpToPx(32));
        overlayBackground.setColor(Color.argb(40, 255, 255, 255));
        overlayBackground.setStroke(dpToPx(1), Color.argb(90, 255, 255, 255));
        binding.glassOverlay.setBackground(overlayBackground);

        binding.colorPreview.setClipToOutline(true);
        binding.colorPreview.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(32));
            }
        });
        binding.colorLabel.setText(currentSelection.getName());
    }

    private void speakCurrentColor() {
        if (textToSpeech == null) {
            Toast.makeText(this, "Speech is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        textToSpeech.speak(currentSelection.getName(), TextToSpeech.QUEUE_FLUSH, null, "learn_color");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
