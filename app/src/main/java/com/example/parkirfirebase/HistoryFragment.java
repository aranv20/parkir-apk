package com.example.parkirfirebase;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<ImageModel> imageList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(imageAdapter);

        // Inisialisasi Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ambil data dari Firestore
        db.collection("parking")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Periksa apakah dokumen memiliki field imageUrl dan namaLokasi
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

        return view;
    }
}
