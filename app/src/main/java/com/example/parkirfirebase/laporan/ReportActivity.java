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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf);

        firestore = FirebaseFirestore.getInstance();


        List<String> reportDataList = getIntent().getStringArrayListExtra("reportDataList");

        if (reportDataList != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createPdf(reportDataList);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            Log.e(TAG, "Error: No report data received.");
            // Handle error, maybe show a toast or navigate back
            finish();
        }
    }

    private void generateReport(List<String> reportDataList) {
        // Log untuk memeriksa apakah data tiba dengan benar
        Log.d(TAG, "Received report data: " + reportDataList);

        // Skip pengambilan data dari Firestore karena data sudah ada dalam reportDataList

        // Pengecekan apakah data tidak kosong sebelum membuat PDF
        if (reportDataList != null && !reportDataList.isEmpty()) {
            // Memeriksa dan meminta izin WRITE_EXTERNAL_STORAGE jika belum diberikan
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                createPdf(reportDataList);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            Log.e(TAG, "Error: Data is empty or null.");
        }
    }

    private void createPdf(List<String> data, List<byte[]> imageDataList) {
        try {
            Log.d(TAG, "Data for PDF: " + data);

            String pdfPath = getExternalFilesDir(null) + "/laporan_parkir.pdf";
            OutputStream outputStream = new FileOutputStream(pdfPath);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            document.open();

            for (int i = 0; i < data.size(); i++) {
                String item = data.get(i);

                // Check if the current item is an image
                if (item.startsWith("image:")) {
                    byte[] imageData = imageDataList.get(i);
                    // Add the image to the PDF
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(imageData);
                    document.add(image);
                } else {
                    // Add text data to the PDF
                    document.add(new com.itextpdf.text.Paragraph(item));
                }
            }

            document.close();
            Log.d(TAG, "PDF created successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error creating PDF: " + e.getMessage());
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
                List<String> reportDataList = getIntent().getStringArrayListExtra("reportDataList");
                if (reportDataList != null && !reportDataList.isEmpty()) {
                    createPdf(reportDataList);
                } else {
                    Log.e(TAG, "Error: Data is empty or null.");
                }
            } else {
                // Izin ditolak, tindakan yang sesuai dapat diambil
                Log.e(TAG, "Permission denied. Unable to create PDF.");
                // Handle the denial, show a message, etc.
                finish();
            }
        }
    }
}
