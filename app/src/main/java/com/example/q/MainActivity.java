package com.example.q;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
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

public class MainActivity extends AppCompatActivity {

    EditText qrCodeValueEditText;
    Button startScanButton;
    Button uploadPhotoButton;
    Button historyButton;
    Button copyButton;

    private ActivityResultLauncher<Intent> resultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeValueEditText = findViewById(R.id.qr_code_value_vt);
        startScanButton = findViewById(R.id.start_scan_button);
        uploadPhotoButton = findViewById(R.id.upload_photo_button);
        historyButton = findViewById(R.id.history_button);
        copyButton = findViewById(R.id.copy_button);

        resultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (result.getData() != null) {
                            String data = result.getData().getStringExtra(ScanQrCodeActivity.Constants.QR_CODE_KEY);
                            updateQrCodeEditText(data);

                            Uri selectedImage = result.getData().getData();
                            if (selectedImage != null) {
                                Intent intent = new Intent(MainActivity.this, ScanQrCodeActivity.class);
                                intent.setData(selectedImage);
                                resultLauncher.launch(intent);
                            }
                        }
                    }
                }
        );

        initButtonClickListener();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
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
                ClipData clip = ClipData.newPlainText("QR Code Value", textToCopy);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, "Скопійовано в буфер обміну", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Копіювати нічого", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateQrCodeEditText(String data) {
        if (data != null) {
            runOnUiThread(() -> qrCodeValueEditText.setText(data));
        }
    }
}

