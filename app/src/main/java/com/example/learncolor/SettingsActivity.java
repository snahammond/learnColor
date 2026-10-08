package com.example.learncolor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    public static final String EXTRA_SELECTED_MODE = "selected_mode";

    private int selectedMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        selectedMode = getIntent().getIntExtra(EXTRA_SELECTED_MODE, 0);

        Button backButton = findViewById(R.id.settingsBackButton);
        Button basicButton = findViewById(R.id.settingsBasicButton);
        Button advancedButton = findViewById(R.id.settingsAdvancedButton);
        Button saveButton = findViewById(R.id.settingsSaveButton);

        backButton.setOnClickListener(v -> finish());
        updateModeButtons(basicButton, advancedButton);

        basicButton.setOnClickListener(v -> {
            selectedMode = 0;
            updateModeButtons(basicButton, advancedButton);
        });

        advancedButton.setOnClickListener(v -> {
            selectedMode = 1;
            updateModeButtons(basicButton, advancedButton);
        });

        saveButton.setOnClickListener(v -> {
            Intent result = new Intent();
            result.putExtra(EXTRA_SELECTED_MODE, selectedMode);
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
}
