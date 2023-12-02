package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.example.parkirfirebase.laporan.ReportActivity;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ParkingFragment extends Fragment {

    private static final String TAG = "ParkingFragment";
    private MainActivity mainActivity;
    private FloatingActionButton floatingActionButton;
    private ExtendedFloatingActionButton extendedFloatingActionButton;
    private FirebaseFirestore firestore;

    public ParkingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking, container, false);
        mainActivity = (MainActivity) getActivity();
        floatingActionButton = view.findViewById(R.id.btn_tambah);
        extendedFloatingActionButton = view.findViewById(R.id.btn_laporan);

        firestore = FirebaseFirestore.getInstance();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddActivity.class);
                startActivity(intent);
            }
        });

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