package com.example.parkirfirebase;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class AddActivity extends AppCompatActivity {

    private Spinner lokasi;
    private FirebaseStorage firebaseStorage;
    private StorageReference mStorageRef;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ImageView gambar, qrCode, imageView;
    private Button upload;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> pilok = new ArrayList<>();
    private ProgressDialog progressDialog;
    private QuerySnapshot cities = null;
    private Bitmap capturedImage;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private boolean isQRCodeGenerated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        firebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = firebaseStorage.getReference();

        lokasi = findViewById(R.id.lokasi);
        imageView = findViewById(R.id.gambar);
        qrCode = findViewById(R.id.qrCode);
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

        imageView.setOnClickListener(new View.OnClickListener() {
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
                if (!isQRCodeGenerated) {
                    if (capturedImage != null) {
                        String lokasiString = lokasi.getSelectedItem().toString();

                        try {
                            uploadToFirebase(capturedImage, lokasiString, new Date());
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(AddActivity.this, "Gagal mengunggah gambar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(AddActivity.this, "Ambil gambar terlebih dahulu sebelum mengunggah.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddActivity.this, "Data sudah diunggah sebelumnya.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Fungsi untuk menghasilkan QR Code
    private Bitmap generateQRCode(String combinedInfo, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(combinedInfo, BarcodeFormat.QR_CODE, width, height);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fungsi untuk menghasilkan dan menampilkan QR Code
    private void generateAndDisplayQRCode(String lokasi, String imageUrl, Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z", Locale.getDefault());
        String formattedDate = dateFormat.format(date);
        String combinedInfo = lokasi + "_" + imageUrl + "_" + formattedDate;

        try {
            Bitmap qrBitmap = generateQRCode(combinedInfo, 300, 300);

            if (qrBitmap != null) {
                qrCode.setImageBitmap(qrBitmap);
                shareQRCode(qrBitmap, formattedDate);
            } else {
                Toast.makeText(AddActivity.this, "Gagal membuat QR code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AddActivity.this, "Gagal membuat QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Fungsi untuk mendapatkan data dari Firestore
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

    // Fungsi untuk memulai kamera
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
                Bitmap capturedImage = (Bitmap) data.getExtras().get("data");

                // Ubah ukuran gambar yang diambil agar sesuai dengan dimensi ImageView
                int targetWidth = imageView.getWidth();
                int targetHeight = imageView.getHeight();
                Bitmap resizedImage = resizeBitmap(capturedImage, targetWidth, targetHeight);

                // Tetapkan gambar yang telah diubah ukurannya ke ImageView
                imageView.setImageBitmap(resizedImage);

                // Simpan gambar yang telah diubah ukurannya
                this.capturedImage = resizedImage;
            }
        }
    }

    // Fungsi untuk mengunggah gambar ke Firebase Storage
    private void uploadToFirebase(Bitmap bitmap, String lokasi, Date date) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, baos);
        byte[] data = baos.toByteArray();

        String timestamp = String.valueOf(System.currentTimeMillis());
        StorageReference imageRef = mStorageRef.child("images/" + timestamp + ".jpeg");

        imageRef.putBytes(data)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveLocationData(lokasi, date, uri.toString());
                    }).addOnFailureListener(e -> {
                        Toast.makeText(AddActivity.this, "Gagal mendapatkan URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("FirebaseStorage", "Gagal mendapatkan URL: " + e.getMessage());
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Gagal upload: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("FirebaseStorage", "Gagal upload: " + e.getMessage());
                });
    }

    // Fungsi untuk menyimpan data lokasi ke Firestore
    private void saveLocationData(String lokasi, Date date, String imageUrl) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z", Locale.getDefault());
        String formattedDate = dateFormat.format(date);

        LokasiModel lokasiModel = new LokasiModel(lokasi, imageUrl, date);

        db.collection("parking")
                .add(lokasiModel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddActivity.this, "Sukses Upload", Toast.LENGTH_SHORT).show();
                    generateAndDisplayQRCode(lokasi, imageUrl, date);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Gagal upload lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Gagal upload lokasi: " + e.getMessage());
                });
    }

    // Fungsi untuk membagikan QR Code
    private void shareQRCode(Bitmap bitmap, String formattedDate) {
        try {
            // Hasilkan URI dari bitmap
            Uri uri = getImageUri(bitmap);

            if (uri != null) {
                // Bagikan gambar dengan memberikan izin pada URI
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png");
                shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Cetak log atau tampilkan pesan Toast untuk memahami di mana kesalahan terjadi
                Log.d("ShareQRCode", "Menggunakan URI: " + uri.toString());

                startActivity(Intent.createChooser(shareIntent, "Bagikan Gambar QR Code"));
            } else {
                Toast.makeText(AddActivity.this, "Gagal menyimpan gambar QR code", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(AddActivity.this, "Kesalahan saat membagikan QR code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Fungsi untuk menghasilkan URI dari bitmap
    private Uri getImageUri(Bitmap bitmap) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "QRCode_" + System.currentTimeMillis(), null);
            return Uri.parse(path);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Fungsi untuk mengubah ukuran bitmap sesuai dengan lebar dan tinggi yang ditentukan
    private Bitmap resizeBitmap(Bitmap bitmap, int width, int height) {
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }
}