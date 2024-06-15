package com.example.q;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import android.content.Intent;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {

    TextView qrCodeValueViewText;
    Button startScanButton;


    private ActivityResultLauncher<Intent> resultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    String data = result.getData().getStringExtra(ScanQrCodeActivity.Constants.QR_CODE_KEY);
                    updateQrCodeViewText(data);
                }
            });

    //оновлення тексту в QrCodeValueViewText
    private void updateQrCodeViewText(String data) {
        if (data != null) {
            runOnUiThread(() -> qrCodeValueViewText.setText(data));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        qrCodeValueViewText = findViewById(R.id.qr_code_value_vt);
        startScanButton = findViewById(R.id.start_scan_button);
        initButtonClickListener();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

    }

        private void initButtonClickListener () {
            startScanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ScanQrCodeActivity.class);
                    resultLauncher.launch(intent);
                }
            });
        }


    }