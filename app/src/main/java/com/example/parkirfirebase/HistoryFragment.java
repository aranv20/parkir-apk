package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        // Inisialisasi firestore
        firestore = FirebaseFirestore.getInstance();

        recyclerView = view.findViewById(R.id.recycler_view);
        mainActivity = (MainActivity) getActivity();
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(imageAdapter);



        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("parking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (document.contains("imageUrl") && document.contains("namaLokasi")) {
                                String imageUrl = document.getString("imageUrl");
                                String namaLokasi = document.getString("namaLokasi");

                                // Tambahkan ke daftar hanya jika kedua field ada
                                ImageModel imageModel = new ImageModel(imageUrl, namaLokasi);
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
                        Log.e("HistoryFragment", "Error getting documents: ", task.getException());
                    }
                });


        extendedFloatingActionButton = view.findViewById(R.id.btn_laporan);
        extendedFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDFAndDownload();
            }
        });

        return view;
}

    private void generatePDFAndDownload() {
        firestore.collection("parking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> reportDataList = new ArrayList<>();
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            String imageUrl = documentSnapshot.getString("imageUrl");
                            String namaLokasi = documentSnapshot.getString("namaLokasi");

                            // Pengecekan apakah timestamp tidak null sebelum memanggil toDate()
                            Timestamp timestamp = documentSnapshot.getTimestamp("waktu_field");
                            String waktu = (timestamp != null) ? timestamp.toDate().toString() : "Unknown";

                            reportDataList.add("Data dari Firestore: " + imageUrl + "\n" +
                                    "Data dari Firestore: " + namaLokasi + "\n" +
                                    "Waktu Real-time: " + waktu + "\n\n");
                        }
                        // Kirim data ke PdfActivity
                        sendReportDataToPdfActivity(reportDataList);
                    } else {
                        // Handle error
                    }
                });
    }


    private void sendReportDataToPdfActivity(List<String> reportDataList) {
        Log.d(TAG, "Sending report data to ReportActivity: " + reportDataList);
        Intent pdfIntent = new Intent(getActivity(), ReportActivity.class);
        pdfIntent.putStringArrayListExtra("reportDataList", (ArrayList<String>) reportDataList);
        startActivity(pdfIntent);
    }
}
