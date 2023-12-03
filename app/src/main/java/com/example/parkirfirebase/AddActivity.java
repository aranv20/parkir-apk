package com.example.parkirfirebase;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.parkirfirebase.history.LokasiModel;
import com.example.parkirfirebase.printqr.PrintBT;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class AddActivity extends AppCompatActivity {

    private Spinner lokasi;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView gambar, qrCode;
    private Button cameraBtn, upload;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> pilok = new ArrayList<>();
    private ProgressDialog progressDialog;
    private QuerySnapshot cities = null;
    private Bitmap capturedImage; // Menyimpan gambar yang diambil dari kamera
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private PrintBT printBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        // Inisialisasi objek printBT
        printBT = new PrintBT();

        firebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = firebaseStorage.getReference();

        cameraBtn = findViewById(R.id.cameraBtn);
        lokasi = findViewById(R.id.lokasi);
        gambar = findViewById(R.id.gambar);
        qrCode = findViewById(R.id.qrCode);
        ImageView imageView = findViewById(R.id.qrCode);
        progressDialog = new ProgressDialog(AddActivity.this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Tunggu sebentar...");

        upload = findViewById(R.id.upload);

        lokasi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                if (cities != null && cities.getDocuments().size() > i) {
                    Log.e("ID CITY", cities.getDocuments().get(i).getId());
                } else {
                    Log.e("Error", "Data cities null atau index di luar batas.");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        getData();

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ContextCompat.checkSelfPermission(AddActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(AddActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
                } else {
                    startCamera();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (capturedImage != null) {
                    String lokasiString = lokasi.getSelectedItem().toString();

                    try {
                        String imageUrl = uploadToFirebase(capturedImage, lokasiString);
                        // Menggabungkan informasi dari spinner dengan timestamp
                        String combinedInfo = lokasiString + "_" + imageUrl;

                        // Menghasilkan QR code dari informasi yang digabungkan
                        BitMatrix bitMatrix = new MultiFormatWriter().encode(combinedInfo, BarcodeFormat.QR_CODE, 300, 300);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        imageView.setImageBitmap(bitmap);

                        // Perbaiki pemanggilan metode printQRCode
                        Bitmap qrBit = printQRCode(combinedInfo);
                        try {
                            PrintBT printBT = new PrintBT(); // Deklarasikan dan inisialisasikan objek PrintBT
                            printBT.findBT();
                            printBT.openBT();
                            printBT.printQRCode(qrBit.toString());
                            printBT.closeBT();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    } catch (WriterException e) {
                        e.printStackTrace();
                        Toast.makeText(AddActivity.this, "Gagal membuat QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddActivity.this, "Ambil gambar terlebih dahulu sebelum generate QR code.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Bitmap printQRCode(String textToQR) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        try {
            BitMatrix bitMatrix = multiFormatWriter.encode(textToQR, BarcodeFormat.QR_CODE, 300, 300);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void getData() {
        progressDialog.show();
        db.collection("city").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                progressDialog.hide();
                if (queryDocumentSnapshots != null) {
                    Log.d("Firestore", "Data berhasil diambil: " + queryDocumentSnapshots.size() + " dokumen.");
                    cities = queryDocumentSnapshots;
                    if (queryDocumentSnapshots.size() > 0) {
                        pilok.clear();
                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                            pilok.add(doc.getString("name"));
                        }
                        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item, pilok);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        lokasi.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getApplicationContext(), "Data tidak tersedia", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("Firestore", "Data kosong atau null.");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.hide();
                Log.e("Firestore", "Gagal mengambil data: " + e.getLocalizedMessage());
                Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Izin kamera diperlukan untuk mengambil gambar.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_CAPTURE && data != null && data.getExtras() != null) {
                capturedImage = (Bitmap) data.getExtras().get("data");
                gambar.setImageBitmap(capturedImage);
            }
        }
    }

    private String uploadToFirebase(Bitmap bitmap, String lokasi) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        String timestamp = String.valueOf(System.currentTimeMillis());
        StorageReference imageRef = mStorageRef.child("images/" + timestamp + ".jpeg");

        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    // Mendapatkan URL unduhan setelah berhasil upload
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Menggunakan URL HTTPS untuk menyimpan data lokasi
                        saveLocationData(lokasi, uri.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddActivity.this, "Gagal mendapatkan URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseStorage", "Gagal mendapatkan URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Gagal upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseStorage", "Gagal upload: " + e.getMessage());
                });

        // Kembalikan URL gambar QR Code
        return imageRef.toString();
    }

    private void saveLocationData(String lokasi, String imageUrl) {
        db.collection("parking")
                .add(new LokasiModel(lokasi, imageUrl)) // Sesuaikan dengan kelas LokasiModel yang Anda miliki
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddActivity.this, "Sukses Upload", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Gagal upload lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Gagal upload lokasi: " + e.getMessage());
                });
    }
}
