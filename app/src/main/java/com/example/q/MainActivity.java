package com.example.q;

import com.example.q.ScanQrCodeActivity;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.MultiFormatReader;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.net.Uri;
import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity {

    EditText qrCodeValueEditText;
    Button startScanButton;
    Button uploadPhotoButton;
    Button historyButton;
    Button copyButton;

    private ActivityResultLauncher<Intent> resultLauncher;
    BarcodeDetector barcodeDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeValueEditText = findViewById(R.id.qr_code_value_vt);
        startScanButton = findViewById(R.id.start_scan_button);
        uploadPhotoButton = findViewById(R.id.upload_photo_button);
        historyButton = findViewById(R.id.history_button);
        copyButton = findViewById(R.id.copy_button);

        barcodeDetector = BarcodeDetectorProvider.getBarcodeDetector(this);

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            String data = result.getData().getStringExtra(ScanQrCodeActivity.Constants.QR_CODE_KEY);
                            updateQrCodeEditText(data);

                            Uri selectedImage = result.getData().getData();
                            if (selectedImage != null) {
                                try {
                                    InputStream inputStream = getContentResolver().openInputStream(selectedImage);
                                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                                    String decoded = scanQRImage(bitmap);
                                    if (decoded != null) {
                                        updateQrCodeEditText(decoded);
                                    } else {
                                        Toast.makeText(this, "QR код в зображенні не знайдено", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
        );

        initButtonClickListener();
    }

    private void initButtonClickListener() {
        startScanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ScanQrCodeActivity.class);
            resultLauncher.launch(intent);
        });

        uploadPhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            resultLauncher.launch(intent);
        });

        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        copyButton.setOnClickListener(v -> {
            String textToCopy = qrCodeValueEditText.getText().toString();
            if (!textToCopy.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Значення QR коду", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Скопійовано в буфер обміну", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Немає даних для копіювання", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQrCodeEditText(String data) {
        if (data != null) {
            runOnUiThread(() -> qrCodeValueEditText.setText(data));
        }
    }


    private String scanQRImage(Bitmap bMap) {
        String contents = null;

        Frame frame = new Frame.Builder().setBitmap(bMap).build();
        SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

        if (barcodes.size() > 0) {
            Barcode barcode = barcodes.valueAt(0);
            contents = barcode.displayValue;
        } else {
            Log.e("QrTest", "Штрих-коду не знайдено");
        }

        return contents;
    }

}
