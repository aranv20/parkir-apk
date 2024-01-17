package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.widget.SearchView;

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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryFragment extends Fragment {

    private static final String TAG = "HistoryFragment";
    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageModel> imageList;
    private ExtendedFloatingActionButton extendedFloatingActionButton;
    private FirebaseFirestore firestore;
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inisialisasi FirebaseFirestore
        firestore = FirebaseFirestore.getInstance();

        // Inisialisasi RecyclerView
        recyclerView = view.findViewById(R.id.recycler_view);
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(imageAdapter);

        // Inisialisasi SearchView
        searchView = view.findViewById(R.id.searchView);
        searchView.setBackgroundResource(R.color.lavender);
        searchView.setQueryHint("Search");
        searchView.setIconifiedByDefault(false);

        // Set listener untuk melakukan pencarian saat text diubah
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Pencarian langsung saat menekan tombol 'Enter'
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Pencarian saat teks berubah
                performSearch(newText);
                return true;
            }
        });

        // Ambil data dari koleksi "parking" di Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ambil data dari koleksi "parking" di Firestore
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
                            } else {
                                // Tangani jika field tidak lengkap
                                // Misalnya, log atau tampilkan pesan kesalahan
                                // atau tambahkan langkah-langkah yang sesuai.
                            }
                        }

                        // Urutkan imageList berdasarkan waktu terbaru
                        Collections.sort(imageList, new Comparator<ImageModel>() {
                            @Override
                            public int compare(ImageModel image1, ImageModel image2) {
                                SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);

                                Date date1, date2;
                                try {
                                    date1 = dateFormat.parse(image1.getWaktu());
                                    date2 = dateFormat.parse(image2.getWaktu());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    return 0;
                                }

                                // Urutkan secara menurun berdasarkan waktu
                                return date2.compareTo(date1);
                            }
                        });

                        // Beri tahu adapter setelah pengurutan
                        imageAdapter.notifyDataSetChanged();

                    } else {
                        // Tangani kesalahan
                        Log.e(TAG, "Error getting documents: ", task.getException());
                    }
                });

        // Inisialisasi ExtendedFloatingActionButton di luar callback
        extendedFloatingActionButton = view.findViewById(R.id.btn_laporan);
        extendedFloatingActionButton.setOnClickListener(v -> generatePDFAndDownload());

        return view;
    }

    private void performSearch(String query) {
        List<ImageModel> searchResults = new ArrayList<>();

        if (query.isEmpty()) {
            // Jika pencarian kosong, tampilkan kembali history asli
            searchResults.addAll(imageList);
        } else {
            // Lakukan pencarian sesuai dengan query
            for (ImageModel imageModel : imageList) {
                if (imageModel.getNamaLokasi().toLowerCase().contains(query.toLowerCase()) ||
                        imageModel.getWaktu().toLowerCase().contains(query.toLowerCase())) {
                    searchResults.add(imageModel);
                }
            }
        }

        imageAdapter.updateList(searchResults);
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
