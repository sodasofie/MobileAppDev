package com.example.q;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ImageButton;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.FileInputStream;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.*;

import java.io.IOException;
import java.lang.reflect.Field;


public class ScanQrCodeActivity extends AppCompatActivity {

    public static class Constants {
        public static final String QR_CODE_KEY = "qr_code_key";
        public static final int CAMERA_REQUEST_CODE = 23;
    }

    private SurfaceView scanSurfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private boolean isFlashOn = false;
    private ImageButton flashButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        scanSurfaceView = findViewById(R.id.scan_surface_view);
        flashButton = findViewById(R.id.flash_button);
        flashButton.setOnClickListener(v -> toggleFlash());

        initBarcodeDetector();
        initScanSurfaceView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (cameraPermissionGranted(requestCode, grantResults)) {
            startCamera();
        } else {
            Toast.makeText(this, "Камера необхідна для сканування QR- чи штрих- коду.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean cameraPermissionGranted(int requestCode, @NonNull int[] grantResults) {
        return requestCode == Constants.CAMERA_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    private void initBarcodeDetector() {
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();
        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Release resources
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                if (detections != null && detections.getDetectedItems() != null) {
                    SparseArray<Barcode> barcodes = detections.getDetectedItems();
                    if (barcodes.size() > 0) {
                        Barcode barcode = barcodes.valueAt(0);
                        if (barcode != null && barcode.displayValue != null && !barcode.displayValue.isEmpty()) {
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

    private void initScanSurfaceView() {
        scanSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                if (ActivityCompat.checkSelfPermission(ScanQrCodeActivity.this,
                        android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
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
                stopCamera();
            }
        });
    }

    private void startCamera() {
        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true)
                .build();

        try {
            cameraSource.start(scanSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopCamera() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    private void toggleFlash() {
        try {
            Field[] declaredFields = CameraSource.class.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.getType() == Camera.class) {
                    field.setAccessible(true);
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        Camera.Parameters params = camera.getParameters();
                        if (isFlashOn) {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            flashButton.setImageResource(R.drawable.flash_off); // Зображення для вимкненого ліхтаря
                        } else {
                            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                            flashButton.setImageResource(R.drawable.flash_on); // Зображення для увімкненого ліхтаря
                        }
                        camera.setParameters(params);
                        isFlashOn = !isFlashOn;
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraSource != null) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}




