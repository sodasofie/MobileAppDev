package com.example.q;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
//import android.graphics.Camera;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
/*import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.luminance.RGBLuminanceSource;*/
import java.io.FileInputStream;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.*;

import java.io.IOException;


public class ScanQrCodeActivity extends AppCompatActivity {

    public static class Constants {
        public static final String QR_CODE_KEY = "qr_code_key";
        private static final int CAMERA_REQUEST_CODE = 23;
    }

    private SurfaceView scanSurfaceView;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private boolean isFlashOn = false;
    private Camera camera;
    private Camera.Parameters params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        scanSurfaceView = findViewById(R.id.scan_surface_view);
        Button flashButton = findViewById(R.id.flash_button);
        flashButton.setOnClickListener(v -> toggleFlash());

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
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        initCameraSource();
        initScanSurfaceView();

        barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                // Release resources
            }


            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                if (detections != null && detections.getDetectedItems() != null) {
                    Barcode barcode = detections.getDetectedItems().valueAt(0);
                    if (barcode != null && barcode.displayValue != null && !barcode.displayValue.isEmpty()) {
                        onQrCodeScanned(barcode.displayValue);
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
                cameraSource.release();
            }
        });
    }

    private void startCamera() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            cameraSource.start(scanSurfaceView.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String scanQRImage(Bitmap bMap) {
        String contents = null;

        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        try {
            Result result = reader.decode(bitmap);
            contents = result.getText();
        } catch (Exception e) {
            Log.e("QrTest", "Error decoding barcode", e);
        }

        return contents;
    }

    private void toggleFlash() {
        if (cameraSource != null) {
            try {
                cameraSource.stop();
                camera = Camera.open();
                params = camera.getParameters();
                if (isFlashOn) {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                } else {
                    params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                }
                camera.setParameters(params);
                camera.startPreview();
                isFlashOn = !isFlashOn;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}




