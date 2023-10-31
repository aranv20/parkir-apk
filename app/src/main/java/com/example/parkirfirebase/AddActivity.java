package com.example.parkirfirebase;

import androidx.activity.result.ActivityResult;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.UploadTask;

import java.util.Arrays;
import java.util.Base64;

public class AddActivity extends AppCompatActivity {

    private Spinner lokasi;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;
    private FirebaseFirestore firestore;
    private ImageView gambar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        mStorageRef = FirebaseStorage.getInstance().getReference();

        gambar = findViewById(R.id.gambar);
        lokasi = findViewById(R.id.lokasi);
        String[] values={"Pilih Lokasi","Gedung KHD","Gedung Seroja","Gedung Kuliah Baru","Pos Jembatan","Pos Gedung S"};
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
    public void uploadGambar(View v){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,101);
    }
    @Override
    protected void onActivityResult(int requestCode,int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if (resultCode == Activity.RESULT_OK){
            if(requestCode == 101){
                onCaptureImageResult(data);
            }
        }
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap)  data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG,90,bytes);
        byte bb[] = bytes.toByteArray();
        gambar.setImageBitmap(thumbnail);

        uploadToFirebase(bb);
    }

    private void uploadToFirebase(byte[] bb) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        StorageReference sr = mStorageRef.child("images/" + timestamp + ".jpg");

        sr.putBytes(bb).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AddActivity.this, "Sukses Upload", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddActivity.this, "Gagal upload", Toast.LENGTH_SHORT).show();
            }
        });
    }
}