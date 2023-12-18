package com.example.parkirfirebase;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
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
import androidx.print.PrintHelper;

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
import java.io.File;
import java.io.FileOutputStream;
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
    private Button upload, printButton;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> pilok = new ArrayList<>();
    private ProgressDialog progressDialog;
    private QuerySnapshot cities = null;
    private Bitmap capturedImage;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private Bitmap bitmap;
    private boolean isQRCodeGenerated = false;
    private static final int WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST = 1;

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
        printButton = findViewById(R.id.print);

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

        printButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isQRCodeGenerated && bitmap != null) {
                    printQRCode(bitmap);
                } else {
                    Toast.makeText(AddActivity.this, "QR code belum di-generate atau tidak tersedia", Toast.LENGTH_SHORT).show();
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
                printQRCode(qrBitmap);
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
                capturedImage = (Bitmap) data.getExtras().get("data");
                imageView.setImageBitmap(capturedImage);
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

    // Fungsi untuk menyimpan QR Code ke galeri perangkat
    private void saveQRCodeToGallery(Bitmap qrBitmap) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                String folderPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/QR_Codes";
                File folder = new File(folderPath);
                if (!folder.exists()) {
                    folder.mkdirs();
                }

                String filename = "QR_Code_" + System.currentTimeMillis() + ".jpg";
                File file = new File(folder, filename);
                FileOutputStream fos = new FileOutputStream(file);
                qrBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.flush();
                fos.close();

                MediaScannerConnection.scanFile(this, new String[]{file.getPath()}, null,
                        (path, uri) -> {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        });

                Toast.makeText(this, "QR Code berhasil disimpan di galeri", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Gagal menyimpan QR Code ke galeri", Toast.LENGTH_SHORT).show();
            }
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST);
        }
    }

    // Fungsi untuk mencetak QR Code
    private void printQRCode(Bitmap qrBitmap) {
        // Membuat objek PrintHelper
        PrintHelper printHelper = new PrintHelper(AddActivity.this);

        // Menyetel orientasi dan skala gambar yang akan dicetak
        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
        printHelper.setOrientation(PrintHelper.ORIENTATION_PORTRAIT);

        // Mencetak gambar QR code
        printHelper.printBitmap("QR Code", qrBitmap);
    }
}
