package com.example.parkirfirebase;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.parkirfirebase.auth.Login;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    FirebaseAuth auth;
    Button button;
    TextView textView;
    FirebaseUser user;
    ImageView imageView;

    // Referensi ke Firebase Storage
    StorageReference storageReference;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        auth = FirebaseAuth.getInstance();
        button = view.findViewById(R.id.logout);
        textView = view.findViewById(R.id.user_details);
        imageView = view.findViewById(R.id.profile_image);
        user = auth.getCurrentUser();

        // Referensi ke Firebase Storage dengan path 'profile'
        storageReference = FirebaseStorage.getInstance().getReference().child("profile");

        // Cek apakah pengguna sudah login
        if (user == null) {
            // Jika tidak, redirect ke halaman login
            Intent intent = new Intent(getActivity(), Login.class);
            startActivity(intent);
            getActivity().finish();
        } else {
            // Jika sudah login, tampilkan email pengguna
            textView.setText(user.getEmail());
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

            // Upload gambar ke Firebase Storage
            uploadImageToFirebase(imageUri);
        }
    }

    // Metode untuk mengunggah gambar ke Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (user != null) {
            // Buat referensi ke lokasi penyimpanan dengan UID pengguna sebagai nama berkas
            StorageReference userImageRef = storageReference.child(Objects.requireNonNull(user.getUid()));

            // Upload gambar ke Firebase Storage
            userImageRef.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        // Gambar berhasil diunggah
                        // Dapatkan URL gambar yang diunggah (bisa digunakan untuk ditampilkan di aplikasi)
                        userImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            // URL gambar tersedia di sini (uri.toString())
                        });
                    })
                    .addOnFailureListener(e -> {
                        // Gagal mengunggah gambar
                        // Handle error
                    });
        }
    }
}
