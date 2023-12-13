package com.example.parkirfirebase.printqr;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothPrinterHelper {

    private static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 123;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private Context context;

    // Konstruktor menerima Context untuk memastikan penanganan izin yang benar
    public BluetoothPrinterHelper(Context context) {
        this.context = context;
    }

    // Metode untuk memulai koneksi Bluetooth secara asinkron
    public void connectToBluetoothPrinterAsync(String address, OnBluetoothConnectListener listener) {
        // Periksa izin Bluetooth sebelum menjalankan AsyncTask
        if (checkBluetoothPermission()) {
            new ConnectBluetoothTask(listener).execute(address);
        } else {
            // Mintalah izin Bluetooth jika belum diberikan
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);
            // Alternatifnya, Anda dapat memberi tahu pengguna atau mengambil tindakan lain yang sesuai
        }
    }

    // Metode untuk mengecek izin Bluetooth
    private boolean checkBluetoothPermission() {
        // Periksa apakah izin Bluetooth sudah diberikan
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    // AsyncTask untuk menjalankan koneksi Bluetooth secara asinkron
    private class ConnectBluetoothTask extends AsyncTask<String, Void, Boolean> {

        private OnBluetoothConnectListener listener;

        public ConnectBluetoothTask(OnBluetoothConnectListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String address = params[0];

            // Periksa izin sebelum melakukan koneksi Bluetooth
            if (checkBluetoothPermission()) {
                return connectToBluetoothPrinter(address);
            } else {
                // Tangani kasus ketika izin tidak diberikan
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (listener != null) {
                if (success) {
                    listener.onConnectSuccess();
                } else {
                    listener.onConnectFailure();
                }
            }
        }
    }

    // Metode onRequestPermissionsResult untuk menanggapi hasil permintaan izin
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, panggil metode yang memerlukan izin di sini
                // Tambahkan logika yang sesuai dengan kebutuhan aplikasi Anda
            } else {
                // Izin ditolak, lakukan penanganan izin ditolak di sini
                // Tambahkan logika yang sesuai dengan kebutuhan aplikasi Anda
            }
        }
    }

    // Metode untuk menjalankan koneksi Bluetooth
    private boolean connectToBluetoothPrinter(String address) {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

        try {
            socket = device.createRfcommSocketToServiceRecord(uuid);
            socket.connect();
            outputStream = socket.getOutputStream();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Antarmuka untuk mendengarkan koneksi Bluetooth
    public interface OnBluetoothConnectListener {
        void onConnectSuccess();
        void onConnectFailure();
    }

    // Metode untuk mencetak data ke printer Bluetooth
    public void printData(byte[] data) {
        if (outputStream != null) {
            try {
                outputStream.write(data);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
            }
        }
    }

    // Metode untuk menutup koneksi dan sumber daya
    private void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
