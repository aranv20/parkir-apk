package com.example.parkirfirebase.laporan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.parkirfirebase.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ReportActivity extends AppCompatActivity {

    private static final String TAG = "ReportActivity";
    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private FirebaseFirestore firestore;
    private List<String> receivedReportDataList;

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
            finish();
        }
    }

    private void generateReport(List<String> reportDataList) {
        Log.d(TAG, "Data laporan yang diterima: " + reportDataList);

        if (reportDataList != null && !reportDataList.isEmpty()) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Buat salinan dari list untuk menghindari masalah modifikasi selama iterasi
                List<String> updatedReportDataList = new ArrayList<>(receivedReportDataList);

                // Menghitung jumlah gambar yang perlu diunduh
                int totalImages = 0;
                for (String data : updatedReportDataList) {
                    if (data.toLowerCase().contains("https") && (data.toLowerCase().contains(".jpg") || data.toLowerCase().contains(".png"))) {
                        totalImages++;
                    }
                }

                // Mengeksekusi unduhan gambar dan pembuatan PDF setelah semua gambar diunduh
                downloadAndProcessImages(updatedReportDataList, totalImages);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
            }
        } else {
            Log.e(TAG, "Error: Data kosong atau null.");
        }
    }

    private void downloadAndProcessImages(List<String> updatedReportDataList, int totalImages) {
        AtomicInteger imagesDownloaded = new AtomicInteger(0);

        // Iterasi melalui data laporan
        for (int i = 0; i < updatedReportDataList.size(); i++) {
            String data = updatedReportDataList.get(i);
            if (data.toLowerCase().contains("https") && (data.toLowerCase().contains(".jpg") || data.toLowerCase().contains(".png"))) {
                final int finalI = i; // Membuat salinan variabel i yang bersifat final
                // Mengunduh dan memproses gambar
                downloadImageToLocal(data, new ImageDownloadListener() {
                    @Override
                    public void onImageDownloaded(byte[] imageBytes) {
                        // Menambahkan 1 ke jumlah gambar yang sudah diunduh
                        int downloadedCount = imagesDownloaded.incrementAndGet();

                        // Jika semua gambar telah diunduh, buat PDF
                        if (downloadedCount == totalImages) {
                            createPdf(updatedReportDataList);
                        }
                    }

                    @Override
                    public void onImageDownloadFailed() {
                        // Handle failure if needed
                    }
                });
            }
        }

        // Jika tidak ada gambar yang perlu diunduh, langsung buat PDF
        if (totalImages == 0) {
            createPdf(updatedReportDataList);
        }
    }

    private void createPdf(List<String> updatedReportDataList) {
        try {
            Log.d(TAG, "Data untuk PDF: " + updatedReportDataList);

            String pdfFileName = "laporan_parkir.pdf";
            String pdfPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + pdfFileName;

            File pdfDirectory = new File(pdfPath);
            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdirs();
            }

            pdfPath = pdfPath + "/" + pdfFileName;

            OutputStream outputStream = new FileOutputStream(pdfPath);

            com.itextpdf.text.Document document = new com.itextpdf.text.Document();
            com.itextpdf.text.pdf.PdfWriter.getInstance(document, outputStream);
            document.open();

            addHeader(document);

            for (String data : updatedReportDataList) {
                if (data.toLowerCase().contains("https") && (data.toLowerCase().contains(".jpg") || data.toLowerCase().contains(".png"))) {
                    addImageFromUrl(document, data);
                } else {
                    document.add(new com.itextpdf.text.Paragraph(data));
                }
            }

            document.close();
            Log.d(TAG, "PDF berhasil dibuat di: " + pdfPath);
        } catch (Exception e) {
            Log.e(TAG, "Error saat membuat PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addImageFromUrl(com.itextpdf.text.Document document, String imageUrl) throws DocumentException {
        try {
            // Mengunduh gambar dari URL
            byte[] imageBytes = downloadImage(imageUrl);

            // Menyisipkan gambar ke dalam dokumen PDF
            Image image = Image.getInstance(imageBytes);
            image.scaleToFit(400, 400);
            document.add(image);
        } catch (IOException e) {
            Log.e(TAG, "Error saat menambahkan gambar dari URL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void downloadImageToLocal(String imageUrl, ImageDownloadListener listener) {
        try {
            // Mengunduh gambar dari URL
            byte[] imageBytes = downloadImage(imageUrl);

            // Panggil listener untuk memberi tahu bahwa gambar telah diunduh
            listener.onImageDownloaded(imageBytes);
        } catch (Exception e) {
            Log.e(TAG, "Error saat mengunduh dan menyimpan gambar lokal: " + e.getMessage());
            e.printStackTrace();

            // Panggil listener untuk memberi tahu bahwa unduhan gagal
            listener.onImageDownloadFailed();
        }
    }

    private byte[] downloadImage(String imageUrl) throws IOException {
        // Mengunduh gambar dari URL
        URL url = new URL(imageUrl);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.connect();

        // Membaca data gambar
        InputStream inputStream = httpURLConnection.getInputStream();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, bytesRead);
        }

        // Menutup sumber daya
        inputStream.close();
        byteArrayOutputStream.close();
        httpURLConnection.disconnect();

        return byteArrayOutputStream.toByteArray();
    }

    private void addHeader(com.itextpdf.text.Document document) throws com.itextpdf.text.DocumentException {
        com.itextpdf.text.Paragraph header = new com.itextpdf.text.Paragraph();
        header.add(new com.itextpdf.text.Chunk("Laporan Parkir Politeknik Negeri Lampung", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 16, com.itextpdf.text.Font.BOLD)));
        header.add(new com.itextpdf.text.Chunk("\nTanggal: " + getCurrentDate(), new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12, com.itextpdf.text.Font.NORMAL)));
        header.add(new com.itextpdf.text.Chunk("\n ", new com.itextpdf.text.Font(com.itextpdf.text.Font.FontFamily.TIMES_ROMAN, 12, com.itextpdf.text.Font.NORMAL)));
        document.add(header);
    }

    private String getCurrentDate() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (receivedReportDataList != null && !receivedReportDataList.isEmpty()) {
                    createPdf(receivedReportDataList);
                } else {
                    Log.e(TAG, "Error: Data kosong atau null.");
                }
            } else {
                Log.e(TAG, "Izin ditolak. Tidak dapat membuat PDF.");
                finish();
            }
        }
    }

    // Listener untuk menginformasikan ketika gambar telah diunduh
    interface ImageDownloadListener {
        void onImageDownloaded(byte[] imageBytes);

        void onImageDownloadFailed();
    }
}
