package com.example.parkirfirebase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class HomeFragment extends Fragment {

    ImageView imageView, parkingImageView, historyImageView, profileImageView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        imageView = view.findViewById(R.id.home);
        parkingImageView = view.findViewById(R.id.parking);
        historyImageView = view.findViewById(R.id.history);
        profileImageView = view.findViewById(R.id.profile);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFragment(new HomeFragment());
            }
        });

        parkingImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke ParkingFragment saat ImageView "parking" diklik
                loadFragment(new ParkingFragment());
            }
        });

        historyImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke HistoryFragment saat ImageView "history" diklik
                loadFragment(new HistoryFragment());
            }
        });

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pindah ke ProfileFragment saat ImageView "profile" diklik
                loadFragment(new ProfileFragment());
            }
        });

        return view;
    }

    private void loadFragment(Fragment fragment) {
        // Mengakses FragmentManager dari activity yang terkait
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        // Memulai transaksi fragment
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        // Mengganti fragment yang saat ini ditampilkan dengan fragment yang diinginkan
        fragmentTransaction.replace(R.id.frame_layout, fragment);

        // Menambahkan transaksi ke back stack, sehingga dapat kembali ke HomeFragment jika diperlukan
        fragmentTransaction.addToBackStack(null);

        // Menyelesaikan transaksi
        fragmentTransaction.commit();
    }
}
