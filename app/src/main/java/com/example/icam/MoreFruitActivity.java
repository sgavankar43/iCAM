package com.example.icam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MoreFruitActivity extends AppCompatActivity {

    TextView nameTextView, descriptionTextView;
    ImageView productImageView;
    Button homeBtn, recaptureBtn;
    TextToSpeech tts;

    private boolean isHomeTappedOnce = false;
    private boolean isRecaptureTappedOnce = false;
    private final Handler handler = new Handler();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_fruit);

        // Initialize UI
        nameTextView = findViewById(R.id.productNameTextView);
        descriptionTextView = findViewById(R.id.productDescriptionTextView);
        productImageView = findViewById(R.id.productImageView);
        homeBtn = findViewById(R.id.homeBtn);
        recaptureBtn = findViewById(R.id.recaptureBtn);

        // Initialize TTS
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);

                // Proceed only after TTS is ready
                displayProductInfo();
            }
        });

        // HOME BUTTON LOGIC
        homeBtn.setOnClickListener(v -> {
            VibrationHelper.vibrate(this, 50); // Add vibration
            if (isHomeTappedOnce) {
                Intent homeIntent = new Intent(MoreFruitActivity.this, MainActivity.class);
                homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(homeIntent);
                finish();
            } else {
                isHomeTappedOnce = true;
                speak("Home");
                handler.postDelayed(() -> isHomeTappedOnce = false, 1500);
            }
        });

        // RECAPTURE BUTTON LOGIC
        recaptureBtn.setOnClickListener(v -> {
            VibrationHelper.vibrate(this, 50); // Add vibration
            if (isRecaptureTappedOnce) {
                Intent recaptureIntent = new Intent(MoreFruitActivity.this, CameraActivityFruit.class);
                startActivity(recaptureIntent);
                finish();
            } else {
                isRecaptureTappedOnce = true;
                speak("Recapture");
                handler.postDelayed(() -> isRecaptureTappedOnce = false, 1500);
            }
        });
    }

    private void displayProductInfo() {
        Intent intent = getIntent();
        String productName = intent.getStringExtra("product_name");
        byte[] imageBytes = intent.getByteArrayExtra("product_image");

        if (productName != null && !productName.isEmpty()) {
            DatabaseHelperFruits dbHelper = new DatabaseHelperFruits(this);
            DatabaseHelperFruits.ProduceModel product = dbHelper.getProduceByName(productName);

            if (product != null) {
                nameTextView.setText(product.getName());
                descriptionTextView.setText(product.getDescription());

                if (imageBytes != null) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    productImageView.setImageBitmap(bitmap);
                } else {
                    productImageView.setImageResource(R.drawable.ic_launcher_foreground);
                }

                speak(product.getName() + ". " + product.getDescription());
            } else {
                nameTextView.setText("Product not found");
                descriptionTextView.setText("No additional information available.");
                productImageView.setImageResource(R.drawable.ic_launcher_foreground);
                speak("Product not found. No additional information available.");
            }
        } else {
            speak("No product name received");
            finish();
        }
    }

    private void speak(String text) {
        if (tts != null) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
