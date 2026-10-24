package com.example.learncolor;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
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
    private ObjectAnimator pulseAnimator;
    private ObjectAnimator previewBounceAnimator;
    private ObjectAnimator previewRotateAnimator;

    private final List<LearningItem> basicColors = new ArrayList<>(Arrays.asList(
            LearningItem.fromColor("Red", Color.RED, R.drawable.learning_color_red),
            LearningItem.fromColor("Yellow", Color.YELLOW, R.drawable.learning_color_yellow),
            LearningItem.fromColor("Green", Color.GREEN, R.drawable.learning_color_green),
            LearningItem.fromColor("Blue", Color.BLUE, R.drawable.learning_color_blue)
    ));

    private final List<LearningItem> advancedColors = new ArrayList<>(Arrays.asList(
            LearningItem.fromColor("Red", Color.RED, R.drawable.learning_color_red),
            LearningItem.fromColor("Orange", Color.rgb(255, 153, 0), R.drawable.learning_color_orange),
            LearningItem.fromColor("Yellow", Color.YELLOW, R.drawable.learning_color_yellow),
            LearningItem.fromColor("Green", Color.GREEN, R.drawable.learning_color_green),
            LearningItem.fromColor("Blue", Color.BLUE, R.drawable.learning_color_blue),
            LearningItem.fromColor("Purple", Color.rgb(128, 0, 255), R.drawable.learning_color_purple),
            LearningItem.fromColor("Pink", Color.rgb(255, 105, 180), R.drawable.learning_color_pink)
    ));

    private final List<LearningItem> basicNumbers = new ArrayList<>(Arrays.asList(
            LearningItem.fromNumber("1", Color.rgb(255, 120, 120), R.drawable.learning_number_1),
            LearningItem.fromNumber("2", Color.rgb(255, 190, 120), R.drawable.learning_number_2),
            LearningItem.fromNumber("3", Color.rgb(120, 200, 120), R.drawable.learning_number_3),
            LearningItem.fromNumber("4", Color.rgb(120, 170, 255), R.drawable.learning_number_4),
            LearningItem.fromNumber("5", Color.rgb(180, 120, 255), R.drawable.learning_number_5)
    ));

    private final List<LearningItem> advancedNumbers = new ArrayList<>(Arrays.asList(
            LearningItem.fromNumber("1", Color.rgb(255, 120, 120), R.drawable.learning_number_1),
            LearningItem.fromNumber("2", Color.rgb(255, 170, 95), R.drawable.learning_number_2),
            LearningItem.fromNumber("3", Color.rgb(255, 210, 93), R.drawable.learning_number_3),
            LearningItem.fromNumber("4", Color.rgb(120, 200, 120), R.drawable.learning_number_4),
            LearningItem.fromNumber("5", Color.rgb(120, 170, 255), R.drawable.learning_number_5),
            LearningItem.fromNumber("6", Color.rgb(130, 120, 255), R.drawable.learning_number_6),
            LearningItem.fromNumber("7", Color.rgb(215, 120, 180), R.drawable.learning_number_7),
            LearningItem.fromNumber("8", Color.rgb(255, 140, 180), R.drawable.learning_number_8),
            LearningItem.fromNumber("9", Color.rgb(126, 205, 202), R.drawable.learning_number_9),
            LearningItem.fromNumber("10", Color.rgb(180, 120, 95), R.drawable.learning_number_10)
    ));

    private final List<LearningItem> basicShapes = new ArrayList<>(Arrays.asList(
            LearningItem.fromShape("Circle", Color.parseColor("#FF7A59"), R.drawable.learning_shape_circle),
            LearningItem.fromShape("Triangle", Color.parseColor("#F7C948"), R.drawable.learning_shape_triangle),
            LearningItem.fromShape("Square", Color.parseColor("#3CCB7F"), R.drawable.learning_shape_square),
            LearningItem.fromShape("Rectangle", Color.parseColor("#4C8DFF"), R.drawable.learning_shape_rectangle)
    ));

    private final List<LearningItem> advancedShapes = new ArrayList<>(Arrays.asList(
            LearningItem.fromShape("Circle", Color.parseColor("#FF7A59"), R.drawable.learning_shape_circle),
            LearningItem.fromShape("Triangle", Color.parseColor("#F7C948"), R.drawable.learning_shape_triangle),
            LearningItem.fromShape("Square", Color.parseColor("#3CCB7F"), R.drawable.learning_shape_square),
            LearningItem.fromShape("Rectangle", Color.parseColor("#4C8DFF"), R.drawable.learning_shape_rectangle),
            LearningItem.fromShape("Pentagon", Color.parseColor("#A66CFF"), R.drawable.learning_shape_pentagon),
            LearningItem.fromShape("Hexagon", Color.parseColor("#FF5EBC"), R.drawable.learning_shape_hexagon),
            LearningItem.fromShape("Star", Color.parseColor("#5ED7D1"), R.drawable.learning_shape_star)
    ));

    private LearningItemCardView activePreviewCard;
    private Mode currentMode = Mode.BASIC;
    private LearningType currentTopic = LearningType.COLORS;
    private LearningItem currentItem = basicColors.get(0);

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
            speakCurrentItem();
        });

        activePreviewCard = new LearningItemCardView(this, dpToPx(32));
        activePreviewCard.setVisibility(View.VISIBLE);
        ((FrameLayout) binding.colorPreview).addView(activePreviewCard,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        setMode(Mode.BASIC);
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
                currentMode == Mode.BASIC ? 0 : 1);
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
            setMode(selectedMode == 0 ? Mode.BASIC : Mode.ADVANCED);
            stopJingleLoop();
            startJingleLoop();
            startLivingPreviewAnimation();
        }
    }

    private void setMode(Mode mode) {
        currentMode = mode;
        List<LearningItem> options = getCurrentOptions();
        currentItem = options.get(0);
        updateModeControls();
        updateTopicHeader();
        updateOptionStrip();
        updatePreview();
    }

    private void updateTopicHeader() {
        String selectedTopic = getTopicLabel();
        binding.appTitle.setText("Learn " + selectedTopic);
        binding.appTitle.setTextColor(Color.parseColor("#1F2937"));
        binding.appTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 34f);
        binding.appTitle.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        binding.appTitle.setShadowLayer(10f, 0f, 2f, Color.parseColor("#DDE7FF"));
        binding.appTitle.setLetterSpacing(0.02f);
        binding.colorSelectorTitle.setText(getSelectionLabel());
    }

    private String getTopicLabel() {
        if (currentTopic == LearningType.NUMBERS) return "Numbers";
        if (currentTopic == LearningType.SHAPES) return "Shapes";
        return "Color";
    }

    private String getSelectionLabel() {
        if (currentTopic == LearningType.NUMBERS) return "Choose a number";
        if (currentTopic == LearningType.SHAPES) return "Choose a shape";
        return "Choose a color";
    }

    private List<LearningItem> getCurrentOptions() {
        if (currentTopic == LearningType.NUMBERS) {
            return currentMode == Mode.BASIC ? basicNumbers : advancedNumbers;
        }
        if (currentTopic == LearningType.SHAPES) {
            return currentMode == Mode.BASIC ? basicShapes : advancedShapes;
        }
        return currentMode == Mode.BASIC ? basicColors : advancedColors;
    }

    private void updateModeControls() {
        boolean basicSelected = currentMode == Mode.BASIC;

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

    private void updateOptionStrip() {
        binding.colorBar.removeAllViews();
        List<LearningItem> options = getCurrentOptions();
        int swatchHeight = dpToPx(52);
        int cornerRadius = dpToPx(12);

        for (LearningItem choice : options) {
            LearningItemCardView swatch = new LearningItemCardView(this, cornerRadius);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    swatchHeight,
                    1f
            );
            params.setMargins(4, 4, 4, 4);
            swatch.setLayoutParams(params);
            swatch.setAlpha(choice.equals(currentItem) ? 1.0f : 0.96f);
            swatch.bind(choice, choice.equals(currentItem));
            swatch.setOnClickListener(v -> selectLearningItem(choice));
            binding.colorBar.addView(swatch);
        }
    }

    private void selectLearningItem(LearningItem choice) {
        currentItem = choice;
        updateOptionStrip();
        updatePreview();
        speakCurrentItem();
    }

    private void updatePreview() {
        binding.colorLabel.setText(currentItem.getName());
        binding.colorLabel.setVisibility(View.GONE);
        binding.glassOverlay.setVisibility(View.GONE);
        binding.colorPreview.setBackgroundColor(Color.TRANSPARENT);
        binding.colorPreview.setClipToOutline(false);
        binding.colorPreview.setOutlineProvider(null);

        binding.colorPreview.setScaleX(1f);
        binding.colorPreview.setScaleY(1f);
        binding.colorPreview.setTranslationX(0f);
        binding.colorPreview.setTranslationY(0f);
        binding.colorPreview.setRotation(0f);

        if (activePreviewCard != null) {
            activePreviewCard.bind(currentItem, true);
            activePreviewCard.setVisibility(View.VISIBLE);
            activePreviewCard.setScaleX(1f);
            activePreviewCard.setScaleY(1f);
            activePreviewCard.setTranslationX(0f);
            activePreviewCard.setTranslationY(0f);
            activePreviewCard.setRotation(0f);
        }
    }

    private void startLivingPreviewAnimation() {
        if (pulseAnimator != null) pulseAnimator.cancel();
        if (previewBounceAnimator != null) previewBounceAnimator.cancel();
        if (previewRotateAnimator != null) previewRotateAnimator.cancel();

        if (activePreviewCard == null) {
            startJingleLoop();
            return;
        }

        binding.colorPreview.setScaleX(1f);
        binding.colorPreview.setScaleY(1f);
        binding.colorPreview.setTranslationX(0f);
        binding.colorPreview.setTranslationY(0f);
        binding.colorPreview.setRotation(0f);

        activePreviewCard.setScaleX(0.9f);
        activePreviewCard.setScaleY(0.9f);
        activePreviewCard.setPivotX(activePreviewCard.getWidth() / 2f);
        activePreviewCard.setPivotY(activePreviewCard.getHeight() / 2f);

        int animationDuration = resolveBounceDuration(bounceSpeed);

        pulseAnimator = ObjectAnimator.ofPropertyValuesHolder(
                activePreviewCard,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 0.9f, 1.06f, 1.0f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.9f, 1.06f, 1.0f)
        );
        pulseAnimator.setDuration(animationDuration);
        pulseAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        pulseAnimator.setRepeatMode(ObjectAnimator.RESTART);
        pulseAnimator.start();

        previewBounceAnimator = ObjectAnimator.ofPropertyValuesHolder(
                activePreviewCard,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_X, -6f, 8f, -5f, 0f),
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -10f, 6f, 0f)
        );
        previewBounceAnimator.setDuration(animationDuration);
        previewBounceAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        previewBounceAnimator.setRepeatMode(ObjectAnimator.RESTART);
        previewBounceAnimator.start();

        previewRotateAnimator = ObjectAnimator.ofFloat(activePreviewCard, View.ROTATION, -2f, 3f, -1f, 0f);
        previewRotateAnimator.setDuration(Math.max(1000, animationDuration + 200));
        previewRotateAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        previewRotateAnimator.setRepeatMode(ObjectAnimator.RESTART);
        previewRotateAnimator.start();

        startJingleLoop();
    }

    private void stopLivingPreviewAnimation() {
        if (pulseAnimator != null) {
            pulseAnimator.cancel();
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

    private void triggerPartyPopper() {
        if (activePreviewCard == null) {
            return;
        }

        final int[] popColors = new int[] {
                Color.WHITE,
                currentItem.getColor(),
                Color.parseColor("#FFD54F"),
                Color.parseColor("#FF6F61"),
                Color.parseColor("#7C4DFF"),
                Color.parseColor("#4DD0E1")
        };

        int previewWidth = activePreviewCard.getWidth();
        int previewHeight = activePreviewCard.getHeight();
        if (previewWidth <= 0 || previewHeight <= 0) {
            activePreviewCard.post(this::triggerPartyPopper);
            return;
        }

        int centerX = previewWidth / 2;
        int centerY = previewHeight / 2;

        for (int i = 0; i < 30; i++) {
            final View particle = new View(this);
            int size = dpToPx(20 + (i % 7) * 5);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(size, size);
            params.leftMargin = centerX - size / 2;
            params.topMargin = centerY - size / 2;
            particle.setLayoutParams(params);
            particle.setBackground(createCircleParticle(popColors[i % popColors.length]));
            particle.setAlpha(1f);
            activePreviewCard.addView(particle);
            particle.bringToFront();

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
                    activePreviewCard.removeView(particle);
                }
            });
            set.start();
        }
    }

    private GradientDrawable createCircleParticle(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        drawable.setSize(dpToPx(10), dpToPx(10));
        return drawable;
    }

    private void speakCurrentItem() {
        if (textToSpeech == null) {
            Toast.makeText(this, "Speech is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        textToSpeech.setSpeechRate(TODDLER_SPEECH_RATE);
        Bundle speechParams = new Bundle();
        speechParams.putString(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
        textToSpeech.speak(currentItem.getName(), TextToSpeech.QUEUE_FLUSH, speechParams, "learn_color");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

}
