package com.example.icam;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.icam.ml.FruitModelMobilenetv2;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;

public class CameraActivityFMCG extends AppCompatActivity {

    Button selectBtn, predictBtn, captureBtn, moreInfoBtn;
    TextView result;
    ImageView imageView;
    Bitmap bitmap;
    TextToSpeech textToSpeech;

    ActivityResultLauncher<Intent> imagePickerLauncher;
    ActivityResultLauncher<Intent> cameraLauncher;

    private int selectClickCount = 0;
    private int captureClickCount = 0;
    private int predictClickCount = 0;
    private int moreInfoClickCount = 0;
    private final Handler handler = new Handler();

    private String predictedProductName = "";

    private final String[] CLASSES = {
            "Coca Cola Can", "Colgate Maximum Cavity Protection", "Fanta",
            "Kurkure Chutney Chaska", "LU Oreo Biscuit", "LU Prince Biscuit",
            "Lays Masala", "Lifebuoy Total Protect Soap", "Lipton Yellow Label Tea",
            "Nestle Fruita Vitals Red Grapes", "Sunsilk Shampoo Soft & Smooth",
            "Supreme Tea", "Tapal Danedar", "Vaseline Healthy White Lotion"
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fmcg_activity);
        getPermission();

        selectBtn = findViewById(R.id.selectBtn);
        predictBtn = findViewById(R.id.predictBtn);
        captureBtn = findViewById(R.id.captureBtn);
        moreInfoBtn = findViewById(R.id.MoreInfoBtn);
        result = findViewById(R.id.result);
        imageView = findViewById(R.id.imageView);

        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(Locale.US);
            }
        });

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), activityResult -> {
                    if (activityResult.getResultCode() == RESULT_OK && activityResult.getData() != null) {
                        Uri uri = activityResult.getData().getData();
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                            imageView.setImageBitmap(bitmap);
                        } catch (IOException e) {
                            e.printStackTrace();
                            speak("Failed to load image.");
                            result.setText("Failed to load image.");
                        }
                    }
                });

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), resultCamera -> {
                    if (resultCamera.getResultCode() == RESULT_OK && resultCamera.getData() != null) {
                        Bundle extras = resultCamera.getData().getExtras();
                        if (extras != null && extras.get("data") != null) {
                            bitmap = (Bitmap) extras.get("data");
                            imageView.setImageBitmap(bitmap);
                        } else {
                            speak("Failed to capture image.");
                            result.setText("Failed to capture image.");
                        }
                    }
                });

        selectBtn.setOnClickListener(v -> handleButtonClick(selectBtn, "Select Image", () -> {
            VibrationHelper.vibrate(this, 50); // Vibration feedback
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            imagePickerLauncher.launch(intent);
        }));

        captureBtn.setOnClickListener(v -> handleButtonClick(captureBtn, "Capture Image", () -> {
            VibrationHelper.vibrate(this, 50); // Vibration feedback
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(intent);
        }));

        predictBtn.setOnClickListener(v -> handleButtonClick(predictBtn, "Predict", this::onClick));

        moreInfoBtn.setOnClickListener(v -> handleButtonClick(moreInfoBtn, "More Info", () -> {
            VibrationHelper.vibrate(this, 50); // Vibration feedback
            if (!predictedProductName.isEmpty() && bitmap != null) {
                Intent intent = new Intent(CameraActivityFMCG.this, MoreInfoActivity.class);
                intent.putExtra("product_name", predictedProductName);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                intent.putExtra("product_image", byteArray);

                speak("Showing more information about " + predictedProductName);
                startActivity(intent);
            } else {
                speak("Please predict a product first.");
                Toast.makeText(this, "Predict a product first", Toast.LENGTH_SHORT).show();
            }
        }));
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 11);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11 && (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)) {
            speak("Camera permission is required.");
            result.setText("Camera permission is required.");
        }
    }

    private void handleButtonClick(Button button, String label, Runnable action) {
        VibrationHelper.vibrate(this, 50); // ✅ Vibration feedback on button tap


        int currentClickCount;

        if (button == selectBtn) {
            currentClickCount = ++selectClickCount;
        } else if (button == captureBtn) {
            currentClickCount = ++captureClickCount;
        } else if (button == predictBtn) {
            currentClickCount = ++predictClickCount;
        } else {
            currentClickCount = ++moreInfoClickCount;
        }

        if (currentClickCount == 1) {
            speak(label);
            handler.postDelayed(() -> {
                if (button == selectBtn) selectClickCount = 0;
                else if (button == captureBtn) captureClickCount = 0;
                else if (button == predictBtn) predictClickCount = 0;
                else moreInfoClickCount = 0;
            }, 1000);
        } else if (currentClickCount == 2) {
            action.run();
            if (button == selectBtn) selectClickCount = 0;
            else if (button == captureBtn) captureClickCount = 0;
            else if (button == predictBtn) predictClickCount = 0;
            else moreInfoClickCount = 0;
        }
    }

    private void speak(String message) {
        if (textToSpeech != null) {
            textToSpeech.speak(message, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private void onClick() {
        if (bitmap != null) {
            try {
                ByteBuffer inputBuffer = convertBitmapToByteBuffer(bitmap, 160);

                FruitModelMobilenetv2 model = FruitModelMobilenetv2.newInstance(getApplicationContext());

                TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 160, 160, 3}, DataType.FLOAT32);
                inputFeature0.loadBuffer(inputBuffer);

                FruitModelMobilenetv2.Outputs outputs = model.process(inputFeature0);
                TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                float[] probabilities = outputFeature0.getFloatArray();
                int maxPos = 0;
                float maxProb = 0;
                for (int i = 0; i < probabilities.length; i++) {
                    if (probabilities[i] > maxProb) {
                        maxProb = probabilities[i];
                        maxPos = i;
                    }
                }

                float confidence = maxProb * 100;

                if (confidence < 40) {
                    predictedProductName = "";
                    String resultText = "Unknown product (Confidence: " + String.format(Locale.US, "%.2f", confidence) + "%)";
                    result.setText(resultText);
                    speak("Unknown product.");
                } else {
                    predictedProductName = CLASSES[maxPos];
                    DataBaseFinalFmcg dbHelper = new DataBaseFinalFmcg(this);
                    DataBaseFinalFmcg.ProductModel product = dbHelper.getProductByName(predictedProductName);

                    String resultText;
                    if (product != null) {
                        resultText = "Product: " + predictedProductName;
                    } else {
                        resultText = "Prediction: " + predictedProductName + "\nNo additional details found.";
                    }

                    result.setText(resultText);
                    speak(resultText);
                }

                model.close();
            } catch (IOException e) {
                result.setText("Model error: " + e.getMessage());
                speak("Model error");
            }
        } else {
            result.setText("Please select or capture an image first.");
            speak("Please select or capture an image first.");
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap, int inputSize) {
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, inputSize, inputSize, true);
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * inputSize * inputSize * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] pixels = new int[inputSize * inputSize];
        resized.getPixels(pixels, 0, inputSize, 0, 0, inputSize, inputSize);

        for (int pixel : pixels) {
            byteBuffer.putFloat(((pixel >> 16) & 0xFF) / 255.f); // R
            byteBuffer.putFloat(((pixel >> 8) & 0xFF) / 255.f);  // G
            byteBuffer.putFloat((pixel & 0xFF) / 255.f);         // B
        }
        return byteBuffer;
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
