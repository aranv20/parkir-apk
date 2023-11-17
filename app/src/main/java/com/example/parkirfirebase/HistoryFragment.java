package com.example.parkirfirebase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ImageAdapter imageAdapter;
    private List<String> imageUrlList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        imageUrlList = new ArrayList<>();
        imageAdapter = new ImageAdapter(imageUrlList);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(imageAdapter);

        // Inisialisasi FirebaseStorage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference().child("images");

        // Inisialisasi Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ambil data dari Firestore
        db.collection("locations")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Dapatkan URL dari Firestore dan tambahkan ke daftar
                            String imageUrl = document.getString("imageUrl");
                            imageUrlList.add(imageUrl);
                            imageAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Tangani kesalahan
                    }
                });

        // Ambil semua gambar dari Firebase Storage
        storageReference.listAll().addOnSuccessListener(listResult -> {
            for (StorageReference item : listResult.getItems()) {
                // Untuk setiap gambar dalam folder, dapatkan URL dan tambahkan ke daftar
                item.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrlList.add(uri.toString());
                    imageAdapter.notifyDataSetChanged();
                }).addOnFailureListener(exception -> {
                    // Tangani kesalahan untuk gambar tertentu
                });
            }
        }).addOnFailureListener(exception -> {
            // Tangani kesalahan untuk penulisan folder
        });

        return view;
    }
}
