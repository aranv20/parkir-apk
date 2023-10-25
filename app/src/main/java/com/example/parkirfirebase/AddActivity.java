package com.example.parkirfirebase;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class AddActivity extends AppCompatActivity {

    Spinner lokasi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        lokasi = findViewById(R.id.lokasi);
        String[] values={"pilih lokasi","Gedung KHD","Gedung Seroja","Gedung Baru"};
        ArrayList<String> arrayList = new ArrayList<>(Arrays.asList(values));
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item,arrayList);
        lokasi.setAdapter(arrayAdapter);

        lokasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (adapterView.getItemAtPosition(i).toString().equals("Pilih Lokasi")) {
                    String lokasidata = adapterView.getItemAtPosition(i).toString();
                    Toast.makeText(AddActivity.this, "" + lokasidata, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}