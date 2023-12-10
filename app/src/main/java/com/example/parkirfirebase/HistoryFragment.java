package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.parkirfirebase.history.ImageAdapter;
import com.example.parkirfirebase.history.ImageModel;
import com.example.parkirfirebase.laporan.ReportActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private MainActivity mainActivity;

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageModel> imageList;
    private ExtendedFloatingActionButton extendedFloatingActionButton;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inisialisasi FirebaseFirestore
        firestore = FirebaseFirestore.getInstance();

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        mainActivity = (MainActivity) getActivity();
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(imageAdapter);

        // Ambil data dari koleksi "parking" di Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Iterasi setiap dokumen dalam hasil query
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Cek apakah dokumen memiliki field yang diperlukan
                            if (document.contains("imageUrl") && document.contains("namaLokasi") && document.contains("waktu")) {
                                // Ambil data dari dokumen
                                String imageUrl = document.getString("imageUrl");
                                String namaLokasi = document.getString("namaLokasi");

                                // Ambil timestamp dan konversi ke Date
                                Timestamp timestamp = document.getTimestamp("waktu");
                                String waktu = (timestamp != null) ? timestamp.toDate().toString() : "Unknown";

                                // Tambahkan ke daftar jika kedua field ada
                                ImageModel imageModel = new ImageModel(imageUrl, namaLokasi, waktu);
                                imageList.add(imageModel);
                                imageAdapter.notifyDataSetChanged();
                            } else {
                                // Tangani jika field tidak lengkap
                                // Misalnya, log atau tampilkan pesan kesalahan
                                // atau tambahkan langkah-langkah yang sesuai.
                            }
                        }
                    } else {
                        // Tangani kesalahan
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });

        // Inisialisasi ExtendedFloatingActionButton
        extendedFloatingActionButton = view.findViewById(R.id.btn_laporan);
        extendedFloatingActionButton.setOnClickListener(v -> generatePDFAndDownload());

        return view;
    }

    private void generatePDFAndDownload() {
        // Ambil data dari koleksi "parking" di Firestore
        firestore.collection("parking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> reportDataList = new ArrayList<>();
                        // Iterasi setiap dokumen dalam hasil query
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            // Ambil data dari dokumen
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String namaLokasi = documentSnapshot.getString("namaLokasi");

                            // Pengecekan apakah timestamp tidak null sebelum memanggil toDate()
                            Timestamp timestamp = documentSnapshot.getTimestamp("waktu");
                            String waktu = (timestamp != null) ? timestamp.toDate().toString() : "Unknown";

                            // Tambahkan ke daftar untuk PDF
                            reportDataList.add("Gambar: " + imageUrl + "\n" +
                                    "Lokasi: " + namaLokasi + "\n" +
                                    "Waktu Real-time: " + waktu + "\n\n");
                        }

                        // Kirim data ke PdfActivity
                        sendReportDataToPdfActivity(reportDataList);

                        // Tampilkan Toast berhasil
                        showToast("PDF berhasil dibuat");
                    } else {
                        // Tangani kesalahan
                        showToast("Gagal membuat PDF");
                    }
                });
    }

    private void showToast(String message) {
        // Tampilkan Toast dengan pesan
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendReportDataToPdfActivity(List<String> reportDataList) {
        // Log data yang dikirim ke ReportActivity
        Log.d(TAG, "Sending report data to ReportActivity: " + reportDataList);

        // Buat Intent untuk PdfActivity
        Intent pdfIntent = new Intent(getActivity(), ReportActivity.class);

        // Sertakan data PDF dalam Intent
        pdfIntent.putStringArrayListExtra("reportDataList", (ArrayList<String>) reportDataList);

        // Mulai aktivitas PdfActivity
        startActivity(pdfIntent);
    }
}
