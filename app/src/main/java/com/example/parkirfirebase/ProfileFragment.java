package com.example.parkirfirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.parkirfirebase.auth.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseAuth auth;
    Button button;
    TextView textView;
    ImageView imageView;

    // Referensi ke Firebase Storage
    StorageReference storageReference;

    // Referensi ke Realtime Database
    DatabaseReference databaseReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        button = view.findViewById(R.id.logout);
        textView = view.findViewById(R.id.user_details);
        imageView = view.findViewById(R.id.profile_image);

        // Referensi ke Firebase Storage dengan path 'profile'
        storageReference = FirebaseStorage.getInstance().getReference().child("profile");

        // Referensi ke Realtime Database
        databaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        // Mendapatkan pengguna yang terautentikasi
        FirebaseUser user = auth.getCurrentUser();

        // Cek apakah pengguna sudah login
        if (user == null) {
            // Jika tidak, redirect ke halaman login
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            // Jika sudah login, tampilkan email pengguna
            textView.setText(user.getEmail());

            // Muat dan tampilkan gambar profil
            loadProfileImage();
        }

        // Set OnClickListener untuk gambar agar dapat memilih foto profil
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImagePicker();
            }
        });

        // Set OnClickListener untuk tombol logout
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lakukan logout dan redirect ke halaman login
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        return view;
    }

    // Metode untuk membuka pemilih gambar (galeri)
    private void openImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
    }

    // Metode untuk menanggapi hasil pemilihan gambar dari galeri
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            // Dapatkan URI gambar yang dipilih
            Uri imageUri = data.getData();

            // Setel gambar profil dengan URI yang dipilih
            imageView.setImageURI(imageUri);

            // Upload gambar ke Firebase Storage dan simpan URL di Realtime Database
            uploadImageToFirebase(imageUri);
        }
    }

    // Metode untuk mengunggah gambar ke Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (auth.getCurrentUser() != null) {
            // Buat referensi ke lokasi penyimpanan dengan UID pengguna sebagai nama berkas
            StorageReference userImageRef = storageReference.child(auth.getCurrentUser().getUid());

            // Upload gambar ke Firebase Storage
            userImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Gambar berhasil diunggah
                        // Dapatkan URL gambar yang diunggah
                        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // URL gambar tersedia di sini (uri.toString())
                            // Simpan URL gambar ke Realtime Database
                            saveImageUrlToDatabase(uri.toString());
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Gagal mengunggah gambar
                        // Handle error
                        Toast.makeText(getActivity(), "Gagal mengunggah gambar", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Metode untuk menyimpan URL gambar di Realtime Database
    private void saveImageUrlToDatabase(String imageUrl) {
        if (auth.getCurrentUser() != null) {
            // Simpan URL gambar ke dalam Realtime Database dengan UID pengguna sebagai key
            databaseReference.child(auth.getCurrentUser().getUid()).child("imageUrl").setValue(imageUrl)
                    .addOnSuccessListener(aVoid -> {
                        // URL gambar berhasil disimpan
                        Toast.makeText(getActivity(), "Gambar profil berhasil disimpan", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        // Gagal menyimpan URL gambar
                        // Handle error
                        Toast.makeText(getActivity(), "Gagal menyimpan gambar profil", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    // Metode untuk memuat dan menampilkan gambar profil dari URL
    private void loadProfileImage() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && isAdded()) { // Tambahkan pemeriksaan isAdded()
            // Ambil URL gambar dari Realtime Database
            databaseReference.child(currentUser.getUid()).child("imageUrl").get().addOnCompleteListener(task -> {
                if (isAdded()) { // Tambahkan pemeriksaan isAdded() di dalam callback
                    if (task.isSuccessful()) {
                        String imageUrl = task.getResult().getValue(String.class);

                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            // Gunakan Glide untuk memuat dan menampilkan gambar
                            Glide.with(requireContext()).load(imageUrl).into(imageView);
                        } else {
                            Log.d("ProfileFragment", "URL Foto Profil Kosong atau Null");
                        }
                    } else {
                        Log.e("ProfileFragment", "Gagal mengambil URL Foto Profil", task.getException());
                    }
                }
            });
        }
    }
}
