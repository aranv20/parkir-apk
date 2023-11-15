package com.example.parkirfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

public class PrintActivity extends AppCompatActivity {

    TextView lokasiPrint;
    ImageView codePrint;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        lokasiPrint = findViewById(R.id.lokasiPrint);
        codePrint = findViewById(R.id.codePrint);

        String namaLok = getIntent().getStringExtra("lokasi");
        String printCode = getIntent().getStringExtra("qrCode");



    }
}