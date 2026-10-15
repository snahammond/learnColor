package com.example.learncolor;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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
    private static final float TODDLER_SPEECH_RATE = 0.42f;
    private static final float JINGLE_VOLUME = 0.72f;
    private static final float JINGLE_DUCKED_VOLUME = 0.18f;
    private static final int PARTY_POPPER_DURATION_MS = 2400;
    private static final String PREFS_NAME = "learn_color_settings";
    private static final String PREF_JINGLE = "selected_jingle";
    private static final String PREF_BOUNCE_SPEED = "selected_bounce_speed";
    private static final String PREF_TOPIC = "selected_learning_topic";
    private static final int DEFAULT_BOUNCE_SPEED = 5;

    private ActivityMainBinding binding;
    private TextToSpeech textToSpeech;
    private MediaPlayer jinglePlayer;
    private int selectedJingleResId = SettingsActivity.DEFAULT_JINGLE;
    private int bounceSpeed = DEFAULT_BOUNCE_SPEED;
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

    private final List<ColorChoice> basicNumbers = new ArrayList<>(Arrays.asList(
            new ColorChoice("1", Color.rgb(255, 120, 120)),
            new ColorChoice("2", Color.rgb(255, 190, 120)),
            new ColorChoice("3", Color.rgb(120, 200, 120)),
            new ColorChoice("4", Color.rgb(120, 170, 255)),
            new ColorChoice("5", Color.rgb(180, 120, 255))
    ));

    private final List<ColorChoice> advancedNumbers = new ArrayList<>(Arrays.asList(
            new ColorChoice("1", Color.rgb(255, 120, 120)),
            new ColorChoice("2", Color.rgb(255, 170, 95)),
            new ColorChoice("3", Color.rgb(255, 210, 93)),
            new ColorChoice("4", Color.rgb(120, 200, 120)),
            new ColorChoice("5", Color.rgb(120, 170, 255)),
            new ColorChoice("6", Color.rgb(130, 120, 255)),
            new ColorChoice("7", Color.rgb(215, 120, 180)),
            new ColorChoice("8", Color.rgb(255, 140, 180)),
            new ColorChoice("9", Color.rgb(126, 205, 202)),
            new ColorChoice("10", Color.rgb(180, 120, 95))
    ));

    private final List<ColorChoice> basicShapes = new ArrayList<>(Arrays.asList(
            new ColorChoice("Circle", Color.parseColor("#FF7A59")),
            new ColorChoice("Triangle", Color.parseColor("#F7C948")),
            new ColorChoice("Square", Color.parseColor("#3CCB7F")),
            new ColorChoice("Rectangle", Color.parseColor("#4C8DFF"))
    ));

    private final List<ColorChoice> advancedShapes = new ArrayList<>(Arrays.asList(
            new ColorChoice("Circle", Color.parseColor("#FF7A59")),
            new ColorChoice("Triangle", Color.parseColor("#F7C948")),
            new ColorChoice("Square", Color.parseColor("#3CCB7F")),
            new ColorChoice("Rectangle", Color.parseColor("#4C8DFF")),
            new ColorChoice("Pentagon", Color.parseColor("#A66CFF")),
            new ColorChoice("Hexagon", Color.parseColor("#FF5EBC")),
            new ColorChoice("Star", Color.parseColor("#5ED7D1"))
    ));

    private ShapePreviewView shapePreviewView;
    private ColorMode currentMode = ColorMode.BASIC;
    private LearningType currentTopic = LearningType.COLORS;
    private ColorChoice currentSelection = basicColors.get(0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textToSpeech = new TextToSpeech(this, this);
        selectedJingleResId = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_JINGLE, SettingsActivity.DEFAULT_JINGLE);
        bounceSpeed = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_BOUNCE_SPEED, DEFAULT_BOUNCE_SPEED);
        currentTopic = LearningType.values()[getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_TOPIC, LearningType.COLORS.ordinal())];

        binding.modeMenuButton.setOnClickListener(v -> openSettings());
        binding.colorPreview.setOnClickListener(v -> {
            triggerPartyPopper();
            speakCurrentColor();
        });

        shapePreviewView = new ShapePreviewView(this);
        shapePreviewView.setVisibility(View.GONE);
        ((FrameLayout) binding.colorPreview).addView(shapePreviewView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        setMode(ColorMode.BASIC);
        binding.modeOptions.setVisibility(View.GONE);
        startLivingPreviewAnimation();
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.setLanguage(Locale.US);
            textToSpeech.setSpeechRate(TODDLER_SPEECH_RATE);
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {
                    duckJingleForSpeech();
                }

                @Override
                public void onDone(String utteranceId) {
                    restoreJingleVolume();
                }

                @Override
                public void onError(String utteranceId) {
                    restoreJingleVolume();
                }

                @Override
                public void onStop(String utteranceId, boolean interrupted) {
                    restoreJingleVolume();
                }
            });
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
        intent.putExtra(SettingsActivity.EXTRA_SELECTED_TOPIC, currentTopic.ordinal());
        intent.putExtra(SettingsActivity.EXTRA_SELECTED_JINGLE, selectedJingleResId);
        intent.putExtra(SettingsActivity.EXTRA_BOUNCE_SPEED, bounceSpeed);
        startActivityForResult(intent, REQUEST_SETTINGS);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_SETTINGS && resultCode == RESULT_OK && data != null) {
            int selectedMode = data.getIntExtra(SettingsActivity.EXTRA_SELECTED_MODE, 0);
            int selectedTopic = data.getIntExtra(SettingsActivity.EXTRA_SELECTED_TOPIC, currentTopic.ordinal());
            int selectedJingle = data.getIntExtra(SettingsActivity.EXTRA_SELECTED_JINGLE, selectedJingleResId);
            int selectedBounceSpeed = data.getIntExtra(SettingsActivity.EXTRA_BOUNCE_SPEED, bounceSpeed);
            selectedJingleResId = selectedJingle;
            bounceSpeed = selectedBounceSpeed;
            currentTopic = LearningType.values()[Math.max(0, Math.min(LearningType.values().length - 1, selectedTopic))];
            saveSelectedJingle();
            saveBounceSpeed();
            saveSelectedTopic();
            setMode(selectedMode == 0 ? ColorMode.BASIC : ColorMode.ADVANCED);
            stopJingleLoop();
            startJingleLoop();
            startLivingPreviewAnimation();
        }
    }

    private void setMode(ColorMode mode) {
        currentMode = mode;
        List<ColorChoice> options = getCurrentOptions();
        currentSelection = options.get(0);
        updateModeControls();
        updateColorStrip();
        updatePreview();
    }

    private List<ColorChoice> getCurrentOptions() {
        if (currentTopic == LearningType.NUMBERS) {
            return currentMode == ColorMode.BASIC ? basicNumbers : advancedNumbers;
        }
        if (currentTopic == LearningType.SHAPES) {
            return currentMode == ColorMode.BASIC ? basicShapes : advancedShapes;
        }
        return currentMode == ColorMode.BASIC ? basicColors : advancedColors;
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
        List<ColorChoice> options = getCurrentOptions();
        int swatchHeight = dpToPx(42);
        int cornerRadius = dpToPx(10);

        for (ColorChoice choice : options) {
            Button swatch = new Button(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    swatchHeight,
                    1f
            );
            params.setMargins(3, 3, 3, 3);
            swatch.setLayoutParams(params);
            swatch.setPadding(0, 0, 0, 0);
            swatch.setAllCaps(false);
            swatch.setMinWidth(0);
            swatch.setMinHeight(0);
            swatch.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
            swatch.setAlpha(choice.equals(currentSelection) ? 1.0f : 0.94f);
            swatch.setOnClickListener(v -> selectColor(choice));

            GradientDrawable border = new GradientDrawable();
            border.setShape(GradientDrawable.RECTANGLE);
            border.setColor(choice.getColor());
            border.setCornerRadius(cornerRadius);
            border.setStroke(2, choice.equals(currentSelection) ? Color.WHITE : Color.argb(110, 255, 255, 255));
            swatch.setBackground(border);

            if (currentTopic == LearningType.NUMBERS) {
                swatch.setText(choice.getName());
                swatch.setTextColor(isDarkColor(choice.getColor()) ? Color.WHITE : Color.BLACK);
                swatch.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            } else if (currentTopic == LearningType.SHAPES) {
                swatch.setText(getShapeGlyph(choice.getName()));
                swatch.setTextColor(Color.WHITE);
                swatch.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            } else {
                swatch.setText("");
            }

            binding.colorBar.addView(swatch);
        }
    }

    private void selectColor(ColorChoice choice) {
        currentSelection = choice;
        updateColorStrip();
        updatePreview();
        speakCurrentColor();
    }

    private void updatePreview() {
        final int targetColor = currentSelection.getColor();
        binding.colorLabel.setText(currentSelection.getName());
        binding.colorLabel.setIncludeFontPadding(false);
        binding.colorLabel.setSingleLine(true);

        if (currentTopic == LearningType.NUMBERS) {
            binding.colorLabel.setTextColor(Color.parseColor("#111827"));
            binding.colorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 360f);
            binding.colorLabel.setGravity(android.view.Gravity.CENTER);
            binding.colorLabel.setBackground(null);
            binding.colorLabel.setPadding(0, 0, 0, 0);
            binding.colorLabel.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            binding.colorLabel.setAlpha(1f);
            binding.colorLabel.setShadowLayer(0f, 0f, 0f, 0);
            binding.colorLabel.setTranslationY(0f);
            binding.colorLabel.setVisibility(View.VISIBLE);
            shapePreviewView.setVisibility(View.GONE);
            binding.colorPreview.setBackgroundColor(Color.parseColor("#F7F4F0"));
            binding.glassOverlay.setBackgroundColor(Color.TRANSPARENT);
            binding.colorPreview.setClipToOutline(false);
            binding.colorPreview.setOutlineProvider(null);
            animatePreviewToColor(targetColor);
            return;
        }

        if (currentTopic == LearningType.SHAPES) {
            binding.colorLabel.setTextColor(Color.WHITE);
            binding.colorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f);
            binding.colorLabel.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
            binding.colorLabel.setPadding(dpToPx(22), dpToPx(10), dpToPx(22), dpToPx(10));
            binding.colorLabel.setBackground(null);
            binding.colorLabel.setVisibility(View.VISIBLE);
            shapePreviewView.setVisibility(View.VISIBLE);
            shapePreviewView.setShapeName(currentSelection.getName(), currentSelection.getColor());
            binding.colorPreview.setBackgroundColor(currentSelection.getColor());
            binding.glassOverlay.setBackgroundColor(Color.argb(65, 255, 255, 255));
            binding.colorPreview.setClipToOutline(false);
            binding.colorPreview.setOutlineProvider(null);
            animatePreviewToColor(targetColor);
            return;
        }

        shapePreviewView.setVisibility(View.GONE);
        binding.colorLabel.setVisibility(View.VISIBLE);
        binding.colorLabel.setTextColor(Color.WHITE);
        binding.colorLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f);
        binding.colorLabel.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);
        binding.colorLabel.setPadding(dpToPx(22), dpToPx(12), dpToPx(22), dpToPx(12));
        binding.colorLabel.setShadowLayer(0f, 0f, 0f, 0);
        binding.colorPreview.setBackgroundColor(Color.TRANSPARENT);
        binding.glassOverlay.setBackgroundColor(Color.TRANSPARENT);
        animatePreviewToColor(targetColor);
    }

    private void animatePreviewToColor(int targetColor) {
        if (previewAnimator != null) {
            previewAnimator.cancel();
        }

        final int startColor = displayedPreviewColor;
        final int effectiveStart = currentTopic == LearningType.NUMBERS ? Color.parseColor("#F7F4F0") : startColor;
        final int effectiveTarget = currentTopic == LearningType.NUMBERS ? Color.parseColor("#F7F4F0") : targetColor;

        previewAnimator = ValueAnimator.ofFloat(0f, 1f);
        previewAnimator.setDuration(550);
        previewAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());
        previewAnimator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();
            if (currentTopic == LearningType.NUMBERS) {
                applyPreviewColor(Color.parseColor("#F7F4F0"));
                return;
            }
            int blendedColor = (Integer) new ArgbEvaluator().evaluate(fraction, effectiveStart, effectiveTarget);
            applyPreviewColor(blendedColor);
        });
        previewAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                displayedPreviewColor = effectiveTarget;
            }
        });
        previewAnimator.start();
    }

    private void applyPreviewColor(int color) {
        if (currentTopic == LearningType.NUMBERS) {
            binding.colorPreview.setBackgroundColor(Color.parseColor("#F7F4F0"));
            binding.glassOverlay.setBackgroundColor(Color.TRANSPARENT);
            binding.colorPreview.setClipToOutline(false);
            binding.colorPreview.setOutlineProvider(null);
            return;
        }

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

    private int blendWithWhite(int baseColor, float amount) {
        int red = Color.red(baseColor);
        int green = Color.green(baseColor);
        int blue = Color.blue(baseColor);

        int whiteRed = 255;
        int whiteGreen = 255;
        int whiteBlue = 255;

        int blendedRed = (int) (red * (1f - amount) + whiteRed * amount);
        int blendedGreen = (int) (green * (1f - amount) + whiteGreen * amount);
        int blendedBlue = (int) (blue * (1f - amount) + whiteBlue * amount);

        return Color.rgb(blendedRed, blendedGreen, blendedBlue);
    }

    private void startLivingPreviewAnimation() {
        if (shimmerAnimator != null) shimmerAnimator.cancel();
        if (pulseAnimator != null) pulseAnimator.cancel();
        if (labelAnimator != null) labelAnimator.cancel();
        if (previewBounceAnimator != null) previewBounceAnimator.cancel();
        if (previewRotateAnimator != null) previewRotateAnimator.cancel();

        int animationDuration = resolveBounceDuration(bounceSpeed);

        if (currentTopic == LearningType.NUMBERS) {
            binding.colorLabel.setScaleX(1f);
            binding.colorLabel.setScaleY(1f);
            float motionTravel = 6f + (bounceSpeed * 2.4f);
            long motionDuration = Math.max(700, 1700 - (bounceSpeed * 100L));
            labelAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    binding.colorLabel,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.97f, 1.08f, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.97f, 1.08f, 1.0f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -motionTravel, 0f)
            );
            labelAnimator.setDuration(motionDuration);
            labelAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            labelAnimator.setRepeatMode(ObjectAnimator.RESTART);
            labelAnimator.start();
        } else {
            shimmerAnimator = ObjectAnimator.ofFloat(binding.glassOverlay, "translationX", -100f, 100f, -100f);
            shimmerAnimator.setDuration(Math.max(800, animationDuration));
            shimmerAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            shimmerAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            shimmerAnimator.start();

            binding.colorPreview.post(() -> {
                binding.colorPreview.setPivotX(binding.colorPreview.getWidth() / 2f);
                binding.colorPreview.setPivotY(binding.colorPreview.getHeight() / 2f);
                binding.colorPreview.setScaleX(0.35f);
                binding.colorPreview.setScaleY(0.35f);
            });

            pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    binding.colorPreview,
                    PropertyValuesHolder.ofFloat(View.SCALE_X, 0.35f, 1.18f, 0.9f, 1.12f, 1.0f),
                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.35f, 1.18f, 0.9f, 1.12f, 1.0f)
            );
            pulseAnimator.setDuration(animationDuration);
            pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            pulseAnimator.setRepeatMode(ObjectAnimator.RESTART);
            pulseAnimator.start();

            previewBounceAnimator = ObjectAnimator.ofPropertyValuesHolder(
                    binding.colorPreview,
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -8f, 10f, -10f, 6f, 0f),
                    PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -14f, 10f, -6f, 0f)
            );
            previewBounceAnimator.setDuration(animationDuration);
            previewBounceAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            previewBounceAnimator.setRepeatMode(ObjectAnimator.RESTART);
            previewBounceAnimator.start();

            previewRotateAnimator = ObjectAnimator.ofFloat(binding.colorPreview, View.ROTATION, -2f, 3f, -2f, 1f, 0f);
            previewRotateAnimator.setDuration(Math.max(1000, animationDuration + 200));
            previewRotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            previewRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
            previewRotateAnimator.start();

            labelAnimator = ObjectAnimator.ofFloat(binding.colorLabel, "translationY", 0f, -12f, 8f, -6f, 0f);
            labelAnimator.setDuration(Math.max(700, animationDuration - 300));
            labelAnimator.setRepeatCount(ObjectAnimator.INFINITE);
            labelAnimator.setRepeatMode(ObjectAnimator.REVERSE);
            labelAnimator.start();
        }

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
            if (jinglePlayer == null) {
                return;
            }
            jinglePlayer.setLooping(true);
            jinglePlayer.setVolume(JINGLE_VOLUME, JINGLE_VOLUME);
            jinglePlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void duckJingleForSpeech() {
        if (jinglePlayer != null && jinglePlayer.isPlaying()) {
            jinglePlayer.setVolume(JINGLE_DUCKED_VOLUME, JINGLE_DUCKED_VOLUME);
        }
    }

    private void restoreJingleVolume() {
        if (jinglePlayer != null) {
            jinglePlayer.setVolume(JINGLE_VOLUME, JINGLE_VOLUME);
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

    private void saveBounceSpeed() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(PREF_BOUNCE_SPEED, bounceSpeed)
                .apply();
    }

    private void saveSelectedTopic() {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(PREF_TOPIC, currentTopic.ordinal())
                .apply();
    }

    private int resolveBounceDuration(int speed) {
        int clampedSpeed = Math.max(1, Math.min(10, speed));
        return Math.max(600, 1800 - ((clampedSpeed - 1) * 120));
    }

    private String getShapeGlyph(String shapeName) {
        switch (shapeName) {
            case "Circle": return "◯";
            case "Triangle": return "△";
            case "Square": return "□";
            case "Rectangle": return "▭";
            case "Pentagon": return "⬟";
            case "Hexagon": return "⬡";
            case "Star": return "★";
            default: return "◯";
        }
    }

    private void triggerPartyPopper() {
        final int[] popColors = new int[] {
                Color.WHITE,
                currentSelection.getColor(),
                Color.parseColor("#FFD54F"),
                Color.parseColor("#FF6F61"),
                Color.parseColor("#7C4DFF"),
                Color.parseColor("#4DD0E1")
        };

        int previewWidth = binding.colorPreview.getWidth();
        int previewHeight = binding.colorPreview.getHeight();
        int centerX = previewWidth / 2;
        int centerY = previewHeight / 2;

        for (int i = 0; i < 30; i++) {
            final View particle = new View(this);
            int size = dpToPx(20 + (i % 7) * 5);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = centerX - size / 2;
            params.topMargin = centerY - size / 2;
            particle.setLayoutParams(params);
            particle.setBackground(createCircleColor(popColors[i % popColors.length]));
            particle.setAlpha(1f);
            binding.colorPreview.addView(particle);

            float spreadX = previewWidth * 0.9f;
            float spreadY = previewHeight * 0.9f;
            float dx = (float) ((Math.random() * spreadX) - (spreadX / 2f));
            float dy = (float) ((Math.random() * spreadY) - (spreadY / 2f));
            float rotation = (float) ((Math.random() * 540f) - 270f);

            ObjectAnimator xAnimator = ObjectAnimator.ofFloat(particle, View.TRANSLATION_X, 0f, dx);
            ObjectAnimator yAnimator = ObjectAnimator.ofFloat(particle, View.TRANSLATION_Y, 0f, dy);
            ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(particle, View.SCALE_X, 0.8f, 3.2f, 0.2f);
            ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(particle, View.SCALE_Y, 0.8f, 3.2f, 0.2f);
            ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(particle, View.ALPHA, 1f, 0.98f, 0.8f, 0.38f, 0f);
            ObjectAnimator rotationAnimator = ObjectAnimator.ofFloat(particle, View.ROTATION, 0f, rotation);

            xAnimator.setDuration(PARTY_POPPER_DURATION_MS);
            yAnimator.setDuration(PARTY_POPPER_DURATION_MS);
            scaleXAnimator.setDuration(PARTY_POPPER_DURATION_MS);
            scaleYAnimator.setDuration(PARTY_POPPER_DURATION_MS);
            alphaAnimator.setDuration(PARTY_POPPER_DURATION_MS);
            rotationAnimator.setDuration(PARTY_POPPER_DURATION_MS);

            android.animation.AnimatorSet set = new android.animation.AnimatorSet();
            set.playTogether(xAnimator, yAnimator, scaleXAnimator, scaleYAnimator, alphaAnimator, rotationAnimator);
            set.addListener(new android.animation.AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(android.animation.Animator animation) {
                    binding.colorPreview.removeView(particle);
                }
            });
            set.start();
        }
    }

    private GradientDrawable createCircleColor(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setSize(dpToPx(10), dpToPx(10));
        return drawable;
    }

    private void speakCurrentColor() {
        if (textToSpeech == null) {
            Toast.makeText(this, "Speech is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        textToSpeech.setSpeechRate(TODDLER_SPEECH_RATE);
        Bundle speechParams = new Bundle();
        speechParams.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
        textToSpeech.speak(currentSelection.getName(), TextToSpeech.QUEUE_FLUSH, speechParams, "learn_color");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private boolean isDarkColor(int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        double luminance = (0.2126 * red + 0.7152 * green + 0.0722 * blue);
        return luminance < 180;
    }
}
