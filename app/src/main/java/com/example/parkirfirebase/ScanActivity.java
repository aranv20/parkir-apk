package com.example.parkirfirebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class ScanActivity extends CaptureActivity {

    private static final String TAG = ScanActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private TextView scannedDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scannedDataTextView = findViewById(R.id.scanned_data_textview);

        // Check camera permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission if not granted
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Start QR Code scan if camera permission is granted
            startQRCodeScan();
        }
    }

    private void startQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true); // Lock the camera orientation to portrait
        integrator.setPrompt("Arahkan kamera ke QR Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Get QR Code scan result
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Handle canceled scan
                Toast.makeText(this, "Pemindaian dibatalkan", Toast.LENGTH_SHORT).show();
            } else {
                // Handle successful scan
                String scannedData = result.getContents();
                Log.d(TAG, "Hasil Pemindaian: " + scannedData);
                Toast.makeText(this, "Hasil Pemindaian: " + scannedData, Toast.LENGTH_SHORT).show();

                // Set the scanned data to the TextView
                scannedDataTextView.setText(scannedData);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Handle camera permission response
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Start QR Code scan if camera permission is granted
                startQRCodeScan();
            } else {
                // Display message if camera permission is denied
                Toast.makeText(this, "Izin kamera dibutuhkan untuk melakukan pemindaian QR Code", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}