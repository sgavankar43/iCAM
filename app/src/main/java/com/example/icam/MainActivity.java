package com.example.icam;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private TextToSpeech textToSpeech;
    private Button btnFMCG, btnFruitsVeggies, btnAbout;
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_DELAY = 300; // milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textToSpeech = new TextToSpeech(this, this);

        btnFMCG = findViewById(R.id.btnFMCG);
        btnFruitsVeggies = findViewById(R.id.btnFruitsVeggies);
        btnAbout = findViewById(R.id.btnAbout);

        // Setup button with double-tap for FMCG
        setupButtonWithDoubleTap(btnFMCG, "Fast Moving Consumer Goods", "FMCG");

        // Setup button with double-tap for Fruits and Vegetables
        setupButtonWithDoubleTap(btnFruitsVeggies, "Fruits and Vegetables", "FruitsVeggies");

        // Setup About button
        btnAbout.setOnClickListener(v -> {
            VibrationHelper.vibrate(this, 100); // Add vibration here
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_DELAY) {
                speak("iCAM is a mobile app made for visually impaired users. It clicks a picture of a product, fruit, or vegetable, then speaks out the name, price range, quantity, nutrition info, and any allergy warnings. iCAM helps you make informed choices—independently and with confidence.");
            } else {
                speak("About Us");
            }
            lastClickTime = clickTime;
        });

        speak("Welcome to Product Identifier App. Please select an option.");
    }

    private void setupButtonWithDoubleTap(Button button, String buttonName, String category) {
        button.setOnClickListener(v -> {
            VibrationHelper.vibrate(this, 100); // Add vibration here
            long clickTime = System.currentTimeMillis();
            if (clickTime - lastClickTime < DOUBLE_CLICK_DELAY) {
                speak(buttonName + " selected. Opening.");
                try {
                    // Decide which activity to launch based on category
                    Class<?> targetActivity = null;
                    if (category.equals("FMCG")) {
                        targetActivity = CameraActivityFMCG.class; // Ensure the class name is correct
                    } else if (category.equals("FruitsVeggies")) {
                        targetActivity = CameraActivityFruit.class; // Ensure the class name is correct
                    } else {
                        throw new Exception("Unknown category");
                    }

                    Intent intent = new Intent(MainActivity.this, targetActivity);
                    intent.putExtra("category", category);
                    startActivity(intent);
                } catch (Exception e) {
                    speak("Error opening " + buttonName);
                    e.printStackTrace();
                }
            } else {
                speak(buttonName);
            }
            lastClickTime = clickTime;
        });
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = textToSpeech.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Language not supported
            } else {
                textToSpeech.setSpeechRate(0.9f);
            }
        }
    }

    private void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "MAIN_UTTERANCE");
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
}
