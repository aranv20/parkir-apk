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

    public BluetoothPrinterHelper(Context context) {
        this.context = context;
    }

    public void connectToBluetoothPrinterAsync(String address, OnBluetoothConnectListener listener) {
        if (checkBluetoothPermission()) {
            new ConnectBluetoothTask(listener).execute(address);
        } else {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }

    private boolean checkBluetoothPermission() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public boolean isBluetoothConnected() {
        return socket != null && socket.isConnected();
    }

    private class ConnectBluetoothTask extends AsyncTask<String, Void, Boolean> {

        private OnBluetoothConnectListener listener;

        public ConnectBluetoothTask(OnBluetoothConnectListener listener) {
            this.listener = listener;
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String address = params[0];
            if (checkBluetoothPermission()) {
                return connectToBluetoothPrinter(address);
            } else {
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

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, lakukan tindakan yang memerlukan izin di sini
            } else {
                // Izin ditolak, lakukan penanganan izin ditolak di sini
            }
        }
    }

    private boolean connectToBluetoothPrinter(String address) {
        if (!isBluetoothSupported()) {
            return false;
        }

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

    public interface OnBluetoothConnectListener {
        void onConnectSuccess();
        void onConnectFailure();
    }

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

    private void closeConnection() {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }
}
