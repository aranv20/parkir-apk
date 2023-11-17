package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class PrintActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        Intent intent = getIntent();
        if (intent != null) {
            String lokasi = intent.getStringExtra("lokasi");
            String qrCodeImageUrl = intent.getStringExtra("qrCodeImageUrl");

            // Tampilkan gambar QR Code menggunakan Picasso atau library lainnya
            ImageView qrCodeImageView = findViewById(R.id.qrCodeImageView);
            Picasso.get().load(qrCodeImageUrl).into(qrCodeImageView);

            // Anda dapat menambahkan logika lainnya di sini berdasarkan kebutuhan
        }
    }
}