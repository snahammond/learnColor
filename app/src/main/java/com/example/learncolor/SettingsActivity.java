package com.example.learncolor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_MODE = "selected_mode";
    public static final String EXTRA_SELECTED_JINGLE = "selected_jingle";
    public static final int DEFAULT_JINGLE = R.raw.simple_radio_jingle_2;

    private int selectedMode;
    private int selectedJingle = DEFAULT_JINGLE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        selectedMode = getIntent().getIntExtra(EXTRA_SELECTED_MODE, 0);
        selectedJingle = getIntent().getIntExtra(EXTRA_SELECTED_JINGLE, DEFAULT_JINGLE);

        Button backButton = findViewById(R.id.settingsBackButton);
        Button basicButton = findViewById(R.id.settingsBasicButton);
        Button advancedButton = findViewById(R.id.settingsAdvancedButton);
        Button saveButton = findViewById(R.id.settingsSaveButton);
        RadioGroup jingleGroup = findViewById(R.id.jingleSelectionGroup);
        RadioButton simpleRadioButton = findViewById(R.id.jingleSimpleRadio);
        RadioButton oceanButton = findViewById(R.id.jingleOceanRadio);
        RadioButton soulfulButton = findViewById(R.id.jingleSoulfulRadio);
        RadioButton syncButton = findViewById(R.id.jingleSyncRadio);

        backButton.setOnClickListener(v -> finish());
        updateModeButtons(basicButton, advancedButton);
        updateJingleSelection(jingleGroup, simpleRadioButton, oceanButton, soulfulButton, syncButton);

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

        saveButton.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTED_MODE, selectedMode);
            result.putExtra(EXTRA_SELECTED_JINGLE, selectedJingle);
            setResult(RESULT_OK, result);
            finish();
        });
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
