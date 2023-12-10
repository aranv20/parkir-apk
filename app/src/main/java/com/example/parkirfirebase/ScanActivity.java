package com.example.parkirfirebase;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.CaptureActivity;

public class ScanActivity extends CaptureActivity {

    public static final String TAG = ScanActivity.class.getSimpleName();
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 200;
    private TextView scannedDataTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        scannedDataTextView = findViewById(R.id.scanned_data_textview);

        // Periksa izin kamera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Minta izin kamera jika belum diberikan
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Mulai pemindaian QR Code jika izin kamera diberikan
            startQRCodeScan();
        }
    }

    private void startQRCodeScan() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true); // Kunci orientasi kamera ke potret
        integrator.setPrompt("Arahkan kamera ke QR Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Dapatkan hasil pemindaian QR Code
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                // Tangani pemindaian yang dibatalkan
                Toast.makeText(this, "Pemindaian dibatalkan", Toast.LENGTH_SHORT).show();
            } else {
                // Tangani pemindaian yang berhasil
                String scannedData = result.getContents();
                Log.d(TAG, "Hasil Pemindaian: " + scannedData);
                Toast.makeText(this, "Hasil Pemindaian: " + scannedData, Toast.LENGTH_SHORT).show();

                // Pisahkan hasil pemindaian menggunakan pemisah tertentu (contohnya, "_")
                String[] parts = scannedData.split("_");
                if (parts.length == 3) {
                    String imageUrl = parts[0];
                    String lokasi = parts[1];
                    String waktu = parts[2];

                    // Tampilkan hasil pisahan
                    String displayText = "Lokasi: " + imageUrl + "\nWaktu: " + waktu;
                    scannedDataTextView.setText(displayText);

                    // Muat dan tampilkan gambar menggunakan Glide
                    ImageView scannedImageView = findViewById(R.id.scanned_image_view);
                    Glide.with(this).load(lokasi).into(scannedImageView);
                } else {
                    // Tangani jika format hasil pemindaian tidak sesuai yang diharapkan
                    Toast.makeText(this, "Format hasil pemindaian tidak sesuai", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Tangani respons izin kamera
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Mulai pemindaian QR Code jika izin kamera diberikan
                startQRCodeScan();
            } else {
                // Tampilkan pesan jika izin kamera ditolak
                Toast.makeText(this, "Izin kamera dibutuhkan untuk melakukan pemindaian QR Code", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
}
