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
import com.example.parkirfirebase.printqr.BluetoothPrinterHelper;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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
    private BluetoothPrinterHelper bluetoothPrinterHelper = new BluetoothPrinterHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        firebaseStorage = FirebaseStorage.getInstance();
        mStorageRef = firebaseStorage.getReference();

        cameraBtn = findViewById(R.id.cameraBtn);
        lokasi = findViewById(R.id.lokasi);
        gambar = findViewById(R.id.gambar);
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
                        String imageUrl = uploadToFirebase(capturedImage, lokasiString, new Date());

                        // Menggabungkan informasi dari spinner dengan timestamp
                        LokasiModel lokasiModel = new LokasiModel(lokasiString, imageUrl, new Date());
                        String combinedInfo = lokasiModel.getNamaLokasi() + "_" + lokasiModel.getImageUrl() + "_" + lokasiModel.getWaktu();

                        // Menghasilkan QR code dari informasi yang digabungkan
                        BitMatrix bitMatrix = new MultiFormatWriter().encode(combinedInfo, BarcodeFormat.QR_CODE, 300, 300);
                        BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                        Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);

                        qrCode.setImageBitmap(bitmap);

                        // Cetak QR Code melalui Bluetooth
                        printQRCodeViaBluetooth(bitmap);
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

    private Bitmap generateQRCode(String combinedInfo, int width, int height) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(combinedInfo, BarcodeFormat.QR_CODE, width, height);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            return barcodeEncoder.createBitmap(bitMatrix);
        } catch (Exception e) {
            e.printStackTrace();
            return null; // or handle the exception as needed
        }
    }

    private void printQRCodeViaBluetooth(Bitmap qrBitmap) {
        // Pastikan untuk mengganti "YOUR_BLUETOOTH_DEVICE_ADDRESS" dengan alamat printer Bluetooth yang sesungguhnya
        String alamatBluetoothPrinter = "86:67:7A:62:7C:0E";
        bluetoothPrinterHelper.connectToBluetoothPrinterAsync(alamatBluetoothPrinter, new BluetoothPrinterHelper.OnBluetoothConnectListener() {
            @Override
            public void onConnectSuccess() {
                // Mengonversi Bitmap ke bentuk byte array (misalnya, format PNG)
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                // Menambahkan pembukaan dan penutupan (contoh: ESC/P Command)
                byte[] openingCommand = new byte[]{0x1B, 0x40};  // ESC @ untuk pembukaan
                byte[] closingCommand = new byte[]{0x1B, 0x4A, 0x00};  // ESC J 0 untuk penutupan

                // Menggabungkan semua data yang akan dicetak
                byte[] dataToPrint = concatenateByteArrays(openingCommand, byteArray, closingCommand);

                // Cetak data byte array
                bluetoothPrinterHelper.printData(dataToPrint);

                // Atur QR code pada ImageView setelah pencetakan berhasil
                qrCode.setImageBitmap(qrBitmap);

                Toast.makeText(AddActivity.this, "QR Code berhasil dicetak", Toast.LENGTH_SHORT).show();
            }

            private byte[] concatenateByteArrays(byte[]... arrays) {
                int totalLength = 0;
                for (byte[] array : arrays) {
                    totalLength += array.length;
                }

                byte[] result = new byte[totalLength];
                int currentIndex = 0;
                for (byte[] array : arrays) {
                    System.arraycopy(array, 0, result, currentIndex, array.length);
                    currentIndex += array.length;
                }

                return result;
            }

            @Override
            public void onConnectFailure() {
                Toast.makeText(AddActivity.this, "Gagal terhubung ke printer Bluetooth", Toast.LENGTH_SHORT).show();
            }
        });
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

    private String uploadToFirebase(Bitmap bitmap, String lokasi, Date date) {
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

        // Kembalikan URL gambar QR Code
        return imageRef.toString();
    }

    private void saveLocationData(String lokasi, Date date, String imageUrl) {
        // Versioning timestamp ke string dengan format tertentu (tanggal-bulan-tahun jam:menit:detik GMT)
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
        String formattedDate = dateFormat.format(date);

        // Menggabungkan informasi lokasi, imageUrl, dan waktu menjadi satu string
        String combinedInfo = lokasi + "_" + imageUrl + "_" + formattedDate;

        // Menghasilkan QR code dari informasi yang digabungkan
        Bitmap bitmap = generateQRCode(combinedInfo, 300, 300);

        // Tampilkan QR code di ImageView
        qrCode.setImageBitmap(bitmap);

        // Membuat objek LokasiModel baru
        LokasiModel lokasiModel = new LokasiModel(lokasi, imageUrl, date);

        // Menyimpan informasi lokasi ke Firestore
        db.collection("parking")
                .add(lokasiModel)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddActivity.this, "Sukses Upload", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddActivity.this, "Gagal upload lokasi: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("Firestore", "Gagal upload lokasi: " + e.getMessage());
                });
    }
}