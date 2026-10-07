package com.example.learncolor;

import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
            new ColorChoice("Blue", Color.BLUE),
            new ColorChoice("Yellow", Color.YELLOW),
            new ColorChoice("Green", Color.GREEN),
            new ColorChoice("Orange", Color.rgb(255, 153, 0)),
            new ColorChoice("Purple", Color.rgb(128, 0, 255)),
            new ColorChoice("Pink", Color.rgb(255, 105, 180)),
            new ColorChoice("Brown", Color.rgb(139, 69, 19)),
            new ColorChoice("Black", Color.BLACK),
            new ColorChoice("White", Color.WHITE),
            new ColorChoice("Gray", Color.GRAY)
    ));

    private final List<ColorChoice> advancedColors = new ArrayList<>(Arrays.asList(
            new ColorChoice("Teal", Color.rgb(0, 128, 128)),
            new ColorChoice("Cyan", Color.CYAN),
            new ColorChoice("Magenta", Color.MAGENTA),
            new ColorChoice("Navy", Color.rgb(0, 0, 128)),
            new ColorChoice("Maroon", Color.rgb(128, 0, 0)),
            new ColorChoice("Olive", Color.rgb(128, 128, 0)),
            new ColorChoice("Gold", Color.rgb(255, 215, 0)),
            new ColorChoice("Silver", Color.rgb(192, 192, 192)),
            new ColorChoice("Lavender", Color.rgb(181, 126, 220)),
            new ColorChoice("Beige", Color.rgb(245, 245, 220))
    ));

    private ColorMode currentMode = ColorMode.BASIC;
    private ColorChoice currentSelection = basicColors.get(0);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        textToSpeech = new TextToSpeech(this, this);

        binding.basicModeButton.setOnClickListener(view -> setMode(ColorMode.BASIC));
        binding.advancedModeButton.setOnClickListener(view -> setMode(ColorMode.ADVANCED));
        binding.speakButton.setOnClickListener(view -> speakCurrentColor());

        binding.colorWheel.setOnColorSelectedListener(color -> updateSelectionFromColor(color));

        setMode(ColorMode.BASIC);
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

    private void setMode(ColorMode mode) {
        currentMode = mode;

        List<ColorChoice> options = (mode == ColorMode.BASIC) ? basicColors : advancedColors;
        currentSelection = options.get(0);

        binding.basicModeButton.setSelected(mode == ColorMode.BASIC);
        binding.advancedModeButton.setSelected(mode == ColorMode.ADVANCED);

        binding.colorWheel.setSelectedColor(currentSelection.getColor());
        updateSelectionFromColor(currentSelection.getColor());
    }

    private void speakCurrentColor() {
        if (textToSpeech == null) {
            Toast.makeText(this, "Speech is not ready yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        String phrase = "The color is " + currentSelection.getName().toLowerCase(Locale.US);
        textToSpeech.speak(phrase, TextToSpeech.QUEUE_FLUSH, null, "learn_color");
    }

    private void updateSelectionFromColor(int color) {
        List<ColorChoice> options = (currentMode == ColorMode.BASIC) ? basicColors : advancedColors;
        currentSelection = findClosestColor(color, options);

        binding.colorPreview.setBackgroundColor(currentSelection.getColor());
        binding.speakButton.setText(currentSelection.getName());
        binding.colorLabel.setText(currentSelection.getName());
        binding.colorWheel.setSelectedColor(currentSelection.getColor());
    }

    private ColorChoice findClosestColor(int targetColor, List<ColorChoice> options) {
        int targetRed = Color.red(targetColor);
        int targetGreen = Color.green(targetColor);
        int targetBlue = Color.blue(targetColor);

        ColorChoice closest = options.get(0);
        int bestDistance = Integer.MAX_VALUE;

        for (ColorChoice choice : options) {
            int redDiff = targetRed - Color.red(choice.getColor());
            int greenDiff = targetGreen - Color.green(choice.getColor());
            int blueDiff = targetBlue - Color.blue(choice.getColor());
            int distance = redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff;

            if (distance < bestDistance) {
                bestDistance = distance;
                closest = choice;
            }
        }

        return closest;
    }
}
