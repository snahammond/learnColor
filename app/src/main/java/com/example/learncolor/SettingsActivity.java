package com.example.learncolor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_MODE = "selected_mode";
    public static final String EXTRA_SELECTED_TOPIC = "selected_topic";
    public static final String EXTRA_SELECTED_JINGLE = "selected_jingle";
    public static final String EXTRA_BOUNCE_SPEED = "bounce_speed";
    public static final String PREFS_NAME = "learn_color_settings";
    public static final String PREF_JINGLE = "selected_jingle";
    public static final String PREF_BOUNCE_SPEED = "selected_bounce_speed";
    public static final int DEFAULT_JINGLE = R.raw.simple_radio_jingle_2;
    public static final int DEFAULT_BOUNCE_SPEED = 5;

    private int selectedMode;
    private int selectedTopic = LearningType.COLORS.ordinal();
    private int selectedJingle = DEFAULT_JINGLE;
    private int selectedBounceSpeed = DEFAULT_BOUNCE_SPEED;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        selectedMode = getIntent().getIntExtra(EXTRA_SELECTED_MODE, 0);
        selectedTopic = getIntent().getIntExtra(EXTRA_SELECTED_TOPIC, LearningType.COLORS.ordinal());
        selectedJingle = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_JINGLE, getIntent().getIntExtra(EXTRA_SELECTED_JINGLE, DEFAULT_JINGLE));
        selectedBounceSpeed = Math.max(1, Math.min(10, getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(PREF_BOUNCE_SPEED, getIntent().getIntExtra(EXTRA_BOUNCE_SPEED, DEFAULT_BOUNCE_SPEED))));

        Button backButton = findViewById(R.id.settingsBackButton);
        Button colorsButton = findViewById(R.id.settingsColorsButton);
        Button numbersButton = findViewById(R.id.settingsNumbersButton);
        Button shapesButton = findViewById(R.id.settingsShapesButton);
        Button basicButton = findViewById(R.id.settingsBasicButton);
        Button advancedButton = findViewById(R.id.settingsAdvancedButton);
        Button saveButton = findViewById(R.id.settingsSaveButton);
        RadioGroup jingleGroup = findViewById(R.id.jingleSelectionGroup);
        RadioButton simpleRadioButton = findViewById(R.id.jingleSimpleRadio);
        RadioButton oceanButton = findViewById(R.id.jingleOceanRadio);
        RadioButton soulfulButton = findViewById(R.id.jingleSoulfulRadio);
        RadioButton syncButton = findViewById(R.id.jingleSyncRadio);
        SeekBar bounceSpeedSlider = findViewById(R.id.bounceSpeedSlider);
        TextView bounceSpeedValue = findViewById(R.id.bounceSpeedValue);

        backButton.setOnClickListener(v -> finish());
        updateTopicButtons(colorsButton, numbersButton, shapesButton);
        updateModeButtons(basicButton, advancedButton);
        updateJingleSelection(jingleGroup, simpleRadioButton, oceanButton, soulfulButton, syncButton);
        updateBounceSpeedValue(bounceSpeedSlider, bounceSpeedValue);

        colorsButton.setOnClickListener(v -> {
            selectedTopic = LearningType.COLORS.ordinal();
            updateTopicButtons(colorsButton, numbersButton, shapesButton);
        });

        numbersButton.setOnClickListener(v -> {
            selectedTopic = LearningType.NUMBERS.ordinal();
            updateTopicButtons(colorsButton, numbersButton, shapesButton);
        });

        shapesButton.setOnClickListener(v -> {
            selectedTopic = LearningType.SHAPES.ordinal();
            updateTopicButtons(colorsButton, numbersButton, shapesButton);
        });

        basicButton.setOnClickListener(v -> {
            selectedMode = 0;
            updateModeButtons(basicButton, advancedButton);
        });

        advancedButton.setOnClickListener(v -> {
            selectedMode = 1;
            updateModeButtons(basicButton, advancedButton);
        });

        jingleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            selectedJingle = getJingleResourceIdForButton(checkedId);
            updateJingleSelection(jingleGroup, simpleRadioButton, oceanButton, soulfulButton, syncButton);
        });

        bounceSpeedSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                selectedBounceSpeed = Math.max(1, Math.min(10, progress + 1));
                updateBounceSpeedLabel(bounceSpeedValue, selectedBounceSpeed);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });
        updateBounceSpeedLabel(bounceSpeedValue, selectedBounceSpeed);

        saveButton.setOnClickListener(v -> {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                    .edit()
                    .putInt(PREF_JINGLE, selectedJingle)
                    .putInt(PREF_BOUNCE_SPEED, selectedBounceSpeed)
                    .putInt("selected_learning_topic", selectedTopic)
                    .apply();

            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTED_MODE, selectedMode);
            result.putExtra(EXTRA_SELECTED_TOPIC, selectedTopic);
            result.putExtra(EXTRA_SELECTED_JINGLE, selectedJingle);
            result.putExtra(EXTRA_BOUNCE_SPEED, selectedBounceSpeed);
            setResult(RESULT_OK, result);
            finish();
        });
    }

    private void updateTopicButtons(Button colorsButton, Button numbersButton, Button shapesButton) {
        boolean colorsSelected = selectedTopic == LearningType.COLORS.ordinal();
        boolean numbersSelected = selectedTopic == LearningType.NUMBERS.ordinal();
        boolean shapesSelected = selectedTopic == LearningType.SHAPES.ordinal();

        colorsButton.setBackgroundColor(colorsSelected ? Color.parseColor("#7C3AED") : Color.parseColor("#F3F4F6"));
        numbersButton.setBackgroundColor(numbersSelected ? Color.parseColor("#7C3AED") : Color.parseColor("#F3F4F6"));
        shapesButton.setBackgroundColor(shapesSelected ? Color.parseColor("#7C3AED") : Color.parseColor("#F3F4F6"));

        colorsButton.setTextColor(colorsSelected ? Color.WHITE : Color.parseColor("#1F2937"));
        numbersButton.setTextColor(numbersSelected ? Color.WHITE : Color.parseColor("#1F2937"));
        shapesButton.setTextColor(shapesSelected ? Color.WHITE : Color.parseColor("#1F2937"));
    }

    private void updateModeButtons(Button basicButton, Button advancedButton) {
        boolean basicSelected = selectedMode == 0;

        basicButton.setBackgroundColor(
                basicSelected ? Color.parseColor("#7C3AED") : Color.parseColor("#E7E7E7")
        );
        advancedButton.setBackgroundColor(
                basicSelected ? Color.parseColor("#E7E7E7") : Color.parseColor("#7C3AED")
        );

        basicButton.setTextColor(basicSelected ? Color.WHITE : Color.parseColor("#1C1C1C"));
        advancedButton.setTextColor(basicSelected ? Color.parseColor("#1C1C1C") : Color.WHITE);
    }

    private void updateBounceSpeedValue(SeekBar bounceSpeedSlider, TextView bounceSpeedValue) {
        int minSpeed = 1;
        int maxSpeed = 10;
        int clampedSpeed = Math.max(minSpeed, Math.min(maxSpeed, selectedBounceSpeed));

        bounceSpeedSlider.setMax(maxSpeed - minSpeed);
        bounceSpeedSlider.setProgress(clampedSpeed - minSpeed);
        updateBounceSpeedLabel(bounceSpeedValue, clampedSpeed);
    }

    private void updateBounceSpeedLabel(TextView bounceSpeedValue, int speed) {
        bounceSpeedValue.setText("Speed: " + speed + "/10");
    }

    private void updateJingleSelection(RadioGroup jingleGroup, RadioButton simpleRadioButton,
                                      RadioButton oceanButton, RadioButton soulfulButton, RadioButton syncButton) {
        simpleRadioButton.setChecked(selectedJingle == R.raw.simple_radio_jingle_2);
        oceanButton.setChecked(selectedJingle == R.raw.oceanframemusic_jingle);
        soulfulButton.setChecked(selectedJingle == R.raw.soulfuljamtracks_jingle);
        syncButton.setChecked(selectedJingle == R.raw.synclabmusic_jingle);
        jingleGroup.check(getButtonIdForJingle(selectedJingle));
    }

    private int getJingleResourceIdForButton(int checkedId) {
        if (checkedId == R.id.jingleOceanRadio) {
            return R.raw.oceanframemusic_jingle;
        }
        if (checkedId == R.id.jingleSoulfulRadio) {
            return R.raw.soulfuljamtracks_jingle;
        }
        if (checkedId == R.id.jingleSyncRadio) {
            return R.raw.synclabmusic_jingle;
        }
        return R.raw.simple_radio_jingle_2;
    }

    private int getButtonIdForJingle(int jingleResourceId) {
        if (jingleResourceId == R.raw.oceanframemusic_jingle) {
            return R.id.jingleOceanRadio;
        }
        if (jingleResourceId == R.raw.soulfuljamtracks_jingle) {
            return R.id.jingleSoulfulRadio;
        }
        if (jingleResourceId == R.raw.synclabmusic_jingle) {
            return R.id.jingleSyncRadio;
        }
        return R.id.jingleSimpleRadio;
    }
}
