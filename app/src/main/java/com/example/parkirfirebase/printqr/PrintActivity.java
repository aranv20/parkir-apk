package com.example.parkirfirebase.printqr;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.parkirfirebase.R;

public class PrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Intent intent = getIntent();
        if (intent != null) {
            String lokasi = intent.getStringExtra("lokasi");
            String qrCodeImageUrl = intent.getStringExtra("qrCodeImageUrl");

            Log.d("PrintActivity", "Menerima Intent - Lokasi: " + lokasi + ", QR Code Image URL: " + qrCodeImageUrl);

            // Anda dapat menambahkan logika lainnya di sini berdasarkan kebutuhan
        }
    }
}