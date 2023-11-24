package com.example.parkirfirebase;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.SparseArray;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ScanActivity extends AppCompatActivity {

    private BarcodeDetector barcodeDetector;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        // Inisialisasi Firebase Firestore
        db = FirebaseFirestore.getInstance();

        // Inisialisasi BarcodeDetector
        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if (!barcodeDetector.isOperational()) {
            Toast.makeText(this, "Barcode detector tidak dapat diinisialisasi. Pastikan Anda terhubung ke internet.", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Mulai pemindaian
        startScanning();
    }

    private void startScanning() {
        // Gunakan kamera untuk pemindaian QR code
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 && resultCode == RESULT_OK && data != null) {
            // Mengambil gambar dari hasil kamera
            BarcodeDetector barcodeDetector = new BarcodeDetector.Builder(this)
                    .setBarcodeFormats(Barcode.QR_CODE)
                    .build();

            if (!barcodeDetector.isOperational()) {
                Toast.makeText(this, "Barcode detector tidak dapat diinisialisasi. Pastikan Anda terhubung ke internet.", Toast.LENGTH_SHORT).show();
                finish();
            }

            if (data.getExtras() != null) {
                Frame frame = new Frame.Builder().setBitmap((Bitmap) data.getExtras().get("data")).build();
                SparseArray<Barcode> barcodes = barcodeDetector.detect(frame);

                if (barcodes.size() > 0) {
                    // QR code ditemukan, ambil nilai teks
                    String qrCodeValue = barcodes.valueAt(0).displayValue;

                    // Ambil data dari Firestore berdasarkan nilai QR code
                    getDataFromFirestore(qrCodeValue);
                } else {
                    Toast.makeText(this, "QR code tidak terdeteksi", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void getDataFromFirestore(String qrCodeValue) {
        // Koneksi ke Firestore dan ambil data berdasarkan nilai QR code
        db.collection("parking")
                .whereEqualTo("qrCodeValue", qrCodeValue) // Sesuaikan dengan struktur data Anda
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Data ditemukan, ambil nilai dari dokumen pertama
                        DocumentSnapshot document = task.getResult().getDocuments().get(0);
                        String lokasi = document.getString("lokasi");
                        String imageUrl = document.getString("imageUrl");

                        // Lakukan sesuatu dengan data yang diambil
                        // Contoh: Tampilkan data di TextView
                        TextView textView = findViewById(R.id.resultTv);
                        textView.setText("Lokasi: " + lokasi + "\nImage URL: " + imageUrl);
                    } else {
                        Toast.makeText(ScanActivity.this, "Data tidak ditemukan di Firestore", Toast.LENGTH_SHORT).show();
                    }

                    // Selesaikan aktivitas setelah mendapatkan data
                    finish();
                });
    }
}
