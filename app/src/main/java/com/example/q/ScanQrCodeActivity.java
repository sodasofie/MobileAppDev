package com.example.q;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import android.content.Intent;

import java.io.IOException;

public class ScanQrCodeActivity extends AppCompatActivity {


    public class Constants {
        public static final String QR_CODE_KEY = "qr_code_key";
        private static final int CAMERA_REQUEST_CODE = 23;
    }

    private SurfaceView scanSurfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        scanSurfaceView = findViewById(R.id.scan_surface_view);
        initBarcodeDetector();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (cameraPermissionGranted(requestCode, grantResults)) {
            Intent intent = new Intent(this, ScanQrCodeActivity.class);
            finish();
            overridePendingTransition(0, 0);
            startActivity(intent);
            overridePendingTransition(0, 0);
        } else {
            Toast.makeText(this, "Камера необхідна для сканування QR- чи штрих- коду.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean cameraPermissionGranted(int requestCode, int[] grantResults) {
        return requestCode == Constants.CAMERA_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }


    private void initBarcodeDetector() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        initCameraSource();
        initScanSurfaceView();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Ваш код для вивільнення ресурсів
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {
                    for (int i = 0; i < barcodes.size(); i++) {
                        Barcode barcode = barcodes.valueAt(i);
                        if (barcode.displayValue != null && !barcode.displayValue.isEmpty()) {
                            onQrCodeScanned(barcode.displayValue);
                        }
                    }
                }
            }
        });
    }

    private void onQrCodeScanned(String value) {
        Intent intent = new Intent();
        intent.putExtra(Constants.QR_CODE_KEY, value);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void initCameraSource() {
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();
    }

    private void initScanSurfaceView() {
        scanSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(ScanQrCodeActivity.this,
                        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        cameraSource.start(scanSurfaceView.getHolder());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    ActivityCompat.requestPermissions(
                            ScanQrCodeActivity.this,
                            new String[]{android.Manifest.permission.CAMERA},
                            Constants.CAMERA_REQUEST_CODE
                    );
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.release();
            }
        });
    }


}