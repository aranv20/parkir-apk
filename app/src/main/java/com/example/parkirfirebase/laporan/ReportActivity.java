package com.example.parkirfirebase.laporan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.parkirfirebase.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = "ReportActivity";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private FirebaseFirestore firestore;
    private List<String> receivedReportDataList; // Deklarasikan sebagai anggota kelas

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        firestore = FirebaseFirestore.getInstance();

        receivedReportDataList = getIntent().getStringArrayListExtra("reportDataList");

        if (receivedReportDataList != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                generateReport(receivedReportDataList);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            Log.e(TAG, "Error: Tidak ada data laporan yang diterima.");
            // Tangani kesalahan, mungkin tampilkan toast atau navigasi kembali
            finish();
        }
    }

    private void generateReport(List<String> reportDataList) {
        // Log untuk memeriksa apakah data tiba dengan benar
        Log.d(TAG, "Data laporan yang diterima: " + reportDataList);

        // Lewati pengambilan data dari Firestore karena data sudah ada dalam reportDataList

        // Periksa apakah data tidak kosong sebelum membuat PDF
        if (reportDataList != null && !reportDataList.isEmpty()) {
            // Periksa dan minta izin WRITE_EXTERNAL_STORAGE jika belum diberikan
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Perhatikan bahwa pada baris berikut, saya mengganti nama argumen dari 'reportDataList' menjadi 'receivedReportDataList'
                createPdf(receivedReportDataList);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            Log.e(TAG, "Error: Data kosong atau null.");
        }
    }

    private void createPdf(List<String> receivedReportDataList) {
        try {
            Log.d(TAG, "Data untuk PDF: " + receivedReportDataList);

            String pdfPath = getExternalFilesDir(null) + "/laporan_parkir.pdf";
            OutputStream outputStream = new FileOutputStream(pdfPath);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            document.open();

            // Implementasi untuk menambahkan data ke PDF
            for (String data : receivedReportDataList) {
                document.add(new com.itextpdf.text.Paragraph(data));
            }

            document.close();
            Log.d(TAG, "PDF berhasil dibuat");
        } catch (Exception e) {
            Log.e(TAG, "Error saat membuat PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Metode untuk menangani respons izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, buat PDF
                if (receivedReportDataList != null && !receivedReportDataList.isEmpty()) {
                    // Perhatikan bahwa pada baris berikut, saya mengganti nama argumen dari 'reportDataList' menjadi 'receivedReportDataList'
                    createPdf(receivedReportDataList);
                } else {
                    Log.e(TAG, "Error: Data kosong atau null.");
                }
            } else {
                // Izin ditolak, tindakan yang sesuai dapat diambil
                Log.e(TAG, "Izin ditolak. Tidak dapat membuat PDF.");
                // Tangani penolakan, tampilkan pesan, dll.
                finish();
            }
        }
    }
}
