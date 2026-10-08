package com.example.learncolor;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
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
    private static final int REQUEST_SETTINGS = 1001;
    private static final float TODDLER_SPEECH_RATE = 0.7f;
    private static final String PREFS_NAME = "learn_color_settings";
    private static final String PREF_JINGLE = "selected_jingle";

    private ActivityMainBinding binding;
    private TextToSpeech textToSpeech;
    private MediaPlayer jinglePlayer;
    private int selectedJingleResId = SettingsActivity.DEFAULT_JINGLE;
    private ObjectAnimator shimmerAnimator;
    private ObjectAnimator pulseAnimator;
    private ObjectAnimator labelAnimator;
    private ObjectAnimator previewBounceAnimator;
    private ObjectAnimator previewRotateAnimator;
    private ValueAnimator previewAnimator;
    private int displayedPreviewColor = Color.RED;

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
        selectedJingleResId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_JINGLE, SettingsActivity.DEFAULT_JINGLE);

        binding.modeMenuButton.setOnClickListener(v -> openSettings());
        binding.colorPreview.setOnClickListener(v -> speakCurrentColor());

        setMode(ColorMode.BASIC);
        binding.modeOptions.setVisibility(View.GONE);
        startLivingPreviewAnimation();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setSpeechRate(TODDLER_SPEECH_RATE);
        }
    }

    @Override
    protected void onPause() {
        stopJingleLoop();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        startJingleLoop();
    }

    @Override
    protected void onDestroy() {
        stopJingleLoop();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        stopLivingPreviewAnimation();
        super.onDestroy();
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(SettingsActivity.EXTRA_SELECTED_MODE,
                currentMode == ColorMode.BASIC ? 0 : 1);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK && data != null) {
            int selectedMode = data.getIntExtra(SettingsActivity.EXTRA_SELECTED_MODE, 0);
            int selectedJingle = data.getIntExtra(SettingsActivity.EXTRA_SELECTED_JINGLE, selectedJingleResId);
            selectedJingleResId = selectedJingle;
            saveSelectedJingle();
            setMode(selectedMode == 0 ? ColorMode.BASIC : ColorMode.ADVANCED);
            stopJingleLoop();
            startJingleLoop();
        }
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

        binding.modeMenuButton.setBackgroundResource(android.R.color.transparent);
        binding.modeMenuButton.setTextColor(Color.parseColor("#1F2937"));
    }

    private void updateColorStrip() {
        binding.colorBar.removeAllViews();
        List<ColorChoice> options = (currentMode == ColorMode.BASIC) ? basicColors : advancedColors;

        for (ColorChoice choice : options) {
            Button swatch = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1f
            );
            params.setMargins(8, 10, 8, 10);
            swatch.setLayoutParams(params);
            swatch.setText("");
            swatch.setPadding(0, 0, 0, 0);
            swatch.setAllCaps(false);
            swatch.setAlpha(choice.equals(currentSelection) ? 1.0f : 0.7f);
            swatch.setOnClickListener(v -> selectColor(choice));

            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.RECTANGLE);
            border.setColor(choice.getColor());
            border.setCornerRadius(dpToPx(16));
            border.setStroke(4, choice.equals(currentSelection) ? Color.WHITE : Color.argb(100, 255, 255, 255));
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
        int targetColor = currentSelection.getColor();
        animatePreviewToColor(targetColor);
        binding.colorLabel.setText(currentSelection.getName());
    }

    private void animatePreviewToColor(int targetColor) {
        if (previewAnimator != null) {
            previewAnimator.cancel();
        }

        final int startColor = displayedPreviewColor;
        previewAnimator = ValueAnimator.ofFloat(0f, 1f);
        previewAnimator.setDuration(550);
        previewAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        previewAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            int blendedColor = (Integer) new ArgbEvaluator().evaluate(fraction, startColor, targetColor);
            applyPreviewColor(blendedColor);
        });
        previewAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                displayedPreviewColor = targetColor;
            }
        });
        previewAnimator.start();
    }

    private void applyPreviewColor(int color) {
        GradientDrawable previewBackground = new GradientDrawable();
        previewBackground.setShape(GradientDrawable.RECTANGLE);
        previewBackground.setColor(color);
        previewBackground.setCornerRadius(dpToPx(32));
        previewBackground.setStroke(dpToPx(2), Color.argb(125, 255, 255, 255));
        binding.colorPreview.setBackground(previewBackground);

        GradientDrawable overlayBackground = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[] {
                        Color.argb(140, 255, 255, 255),
                        Color.argb(35, 255, 255, 255),
                        Color.argb(110, 255, 255, 255)
                }
        );
        overlayBackground.setShape(GradientDrawable.RECTANGLE);
        overlayBackground.setCornerRadius(dpToPx(32));
        overlayBackground.setStroke(dpToPx(1), Color.argb(90, 255, 255, 255));
        binding.glassOverlay.setBackground(overlayBackground);

        binding.colorPreview.setClipToOutline(true);
        binding.colorPreview.setOutlineProvider(new android.view.ViewOutlineProvider() {
            @Override
            public void getOutline(View view, android.graphics.Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), dpToPx(32));
            }
        });
    }

    private void startLivingPreviewAnimation() {
        if (shimmerAnimator != null) shimmerAnimator.cancel();
        if (pulseAnimator != null) pulseAnimator.cancel();
        if (labelAnimator != null) labelAnimator.cancel();
        if (previewBounceAnimator != null) previewBounceAnimator.cancel();
        if (previewRotateAnimator != null) previewRotateAnimator.cancel();

        shimmerAnimator = ObjectAnimator.ofFloat(binding.glassOverlay, "translationX", -100f, 100f, -100f);
        shimmerAnimator.setDuration(1800);
        shimmerAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        shimmerAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        shimmerAnimator.start();

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.colorPreview,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1.0f, 1.22f, 0.96f, 1.12f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1.0f, 1.22f, 0.96f, 1.12f, 1.0f)
        );
        pulseAnimator.setDuration(1000);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ObjectAnimator.RESTART);
        pulseAnimator.start();

        previewBounceAnimator = ObjectAnimator.ofPropertyValuesHolder(
                binding.colorPreview,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -18f, 18f, -24f, 10f, 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -26f, 18f, -10f, 0f)
        );
        previewBounceAnimator.setDuration(1200);
        previewBounceAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        previewBounceAnimator.setRepeatMode(ObjectAnimator.RESTART);
        previewBounceAnimator.start();

        previewRotateAnimator = ObjectAnimator.ofFloat(binding.colorPreview, View.ROTATION, -5f, 6f, -4f, 3f, 0f);
        previewRotateAnimator.setDuration(1500);
        previewRotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        previewRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        previewRotateAnimator.start();

        labelAnimator = ObjectAnimator.ofFloat(binding.colorLabel, "translationY", 0f, -18f, 12f, -8f, 0f);
        labelAnimator.setDuration(900);
        labelAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        labelAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        labelAnimator.start();

        startJingleLoop();
    }

    private void stopLivingPreviewAnimation() {
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
        }
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
        }
        if (labelAnimator != null) {
            labelAnimator.cancel();
        }
        if (previewBounceAnimator != null) {
            previewBounceAnimator.cancel();
        }
        if (previewRotateAnimator != null) {
            previewRotateAnimator.cancel();
        }
    }

    private void startJingleLoop() {
        stopJingleLoop();

        try {
            if (jinglePlayer != null) {
                jinglePlayer.release();
            }

            jinglePlayer = MediaPlayer.create(this, selectedJingleResId);
            jinglePlayer.setLooping(true);
            jinglePlayer.setVolume(0.9f, 0.9f);
            jinglePlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopJingleLoop() {
        if (jinglePlayer != null) {
            if (jinglePlayer.isPlaying()) {
                jinglePlayer.stop();
            }
            jinglePlayer.release();
            jinglePlayer = null;
        }
    }

    private void saveSelectedJingle() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(PREF_JINGLE, selectedJingleResId)
                .apply();
    }

    private void speakCurrentColor() {
        if (textToSpeech == null) {
            Toast.makeText(this, "Speech is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        textToSpeech.setSpeechRate(TODDLER_SPEECH_RATE);
        textToSpeech.speak(currentSelection.getName(), TextToSpeech.QUEUE_FLUSH, null, "learn_color");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
