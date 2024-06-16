package com.example.q;

import android.content.Context;

import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class BarcodeDetectorProvider {
    private static BarcodeDetector barcodeDetector;

    public static BarcodeDetector getBarcodeDetector(Context context) {
        if (barcodeDetector == null) {
            barcodeDetector = new BarcodeDetector.Builder(context)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();
        }
        return barcodeDetector;
    }
}
