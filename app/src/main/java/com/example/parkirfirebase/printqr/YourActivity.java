package com.example.parkirfirebase.printqr;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.parkirfirebase.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class YourActivity extends AppCompatActivity {

    BluetoothDevice yourBluetoothDevice;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final int REQUEST_CODE_BLUETOOTH = 1;
    Bitmap yourBitmap;
    private PrintBT printBT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_print);

        printBT = new PrintBT();

        // Mengecek dan meminta izin BLUETOOTH
        checkBluetoothPermission();

        // Menggunakan Bluetooth (contoh di tombol klik)
        findViewById(R.id.yourButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Sebelum menggunakan Bluetooth, pastikan izinnya telah diberikan
                    if (ContextCompat.checkSelfPermission(YourActivity.this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                        printBT.openBT(yourBluetoothDevice);
                        printBT.printBitmap(yourBitmap);
                    } else {
                        // Jika izin belum diberikan, minta izin kepada pengguna
                        ActivityCompat.requestPermissions(YourActivity.this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_CODE_BLUETOOTH);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    // Tangani kesalahan koneksi Bluetooth di sini
                } finally {
                    // Selalu pastikan untuk menutup koneksi Bluetooth setelah digunakan
                    try {
                        printBT.closeBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // Tangani kesalahan penutupan koneksi Bluetooth di sini
                    }
                }
            }
        });
    }

    private void checkBluetoothPermission() {
        // Memeriksa izin BLUETOOTH
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
            // Izin sudah diberikan, bisa melanjutkan operasi BLUETOOTH
        } else {
            // Jika izin belum diberikan, minta izin kepada pengguna
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH}, REQUEST_CODE_BLUETOOTH);
        }
    }

    // Metode untuk menanggapi hasil permintaan izin
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_BLUETOOTH) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin BLUETOOTH diberikan, bisa melanjutkan operasi BLUETOOTH
            } else {
                // Izin BLUETOOTH ditolak, berikan informasi atau tindakan yang sesuai
            }
        }
    }

    private static class PrintBT {
        private OutputStream outputStream;

        // Metode untuk membuka koneksi Bluetooth
        public void openBT(BluetoothDevice bluetoothDevice) throws IOException {
            // Mendapatkan socket Bluetooth dari implementasi Anda
            BluetoothSocket socket = bluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);

            try {
                // Mencoba menghubungkan socket
                socket.connect();
                outputStream = socket.getOutputStream();

                // Pastikan untuk mengecek apakah outputStream tidak null sebelum digunakan di tempat lain
                if (outputStream == null) {
                    throw new IOException("OutputStream is null");
                }
            } catch (IOException e) {
                // Tangani kesalahan koneksi
                e.printStackTrace();
                try {
                    socket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
                throw e;
            }
        }

        // Metode untuk menutup koneksi Bluetooth
        public void closeBT() throws IOException {
            // Implementasi penutupan koneksi Bluetooth
            // ...
        }

        // Metode untuk mencetak gambar Bitmap
        public void printBitmap(Bitmap bitmap) throws IOException {
            // Implementasi printBitmap
            // ...
        }

        // Metode untuk mengonversi gambar ke array byte sesuai format printer
        private byte[] convertImage(int[] pixels, int width, int height) {
            // Implementasi convertImage
            // ...
            return null;
        }
    }
}
