package com.example.parkirfirebase;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class ParkingFragment extends Fragment {


    MainActivity mainActivity;
    FloatingActionButton floatingActionButton;
    ExtendedFloatingActionButton extendedFloatingActionButton;


    public ParkingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parking, container, false);
        mainActivity = (MainActivity) getActivity();
        floatingActionButton = view.findViewById(R.id.btn_tambah);
        extendedFloatingActionButton = view.findViewById(R.id.btn_laporan);
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
                // Buat instance dari LaporanFragment
                LaporanFragment laporanFragment = new LaporanFragment();

                // Memulai transaksi fragment
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.frame_layout, laporanFragment)  // Ganti R.id.fragment_container dengan ID container fragment Anda
                        .addToBackStack(null)
                        .commit();
            }
        });



    return view;
    }
}