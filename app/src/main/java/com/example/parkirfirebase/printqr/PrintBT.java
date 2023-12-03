package com.example.parkirfirebase.printqr;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.graphics.Bitmap;
import android.graphics.Color;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class PrintBT {

    private OutputStream outputStream;
    private static final UUID MY_UUID = UUID.fromString("your_uuid_here");

    // Metode untuk mencari perangkat Bluetooth
    public void findBT() {
        // Implementasi pencarian perangkat Bluetooth
        // ...
    }

    // Metode untuk membuka koneksi Bluetooth
    // Modifikasi metode openBT agar menerima BluetoothDevice
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

        } finally {
            // Close the socket in a finally block to ensure it gets closed
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }



    // Metode untuk menutup koneksi Bluetooth
    public void closeBT() throws IOException {
        // Implementasi penutupan koneksi Bluetooth
        // ...
    }

    // Metode untuk mencetak gambar Bitmap
    public void printBitmap(Bitmap bitmap) throws IOException {
        // Konversi gambar ke array byte
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        byte[] imageBuffer = convertImage(pixels, width, height);

        // Mengirim perintah mencetak gambar ke printer Bluetooth
        outputStream.write(PrinterCommands.ESC_ALIGN_CENTER);
        outputStream.write(PrinterCommands.SELECT_BIT_IMAGE_MODE);
        outputStream.write(imageBuffer);

        // Memberikan jarak setelah mencetak gambar
        outputStream.write(PrinterCommands.FEED_LINE);

        // Flush output stream untuk memastikan semua data terkirim ke printer
        outputStream.flush();
    }

    // Metode untuk mengonversi gambar ke array byte sesuai format printer
    private byte[] convertImage(int[] pixels, int width, int height) {
        int threshold = 127;
        int index = 0;
        int bitIndex = 0;
        int currentByte = 0;
        int[] pixelsMonochrome = new int[width * height];

        // Konversi gambar ke skala abu-abu (monochrome)
        for (int pixel : pixels) {
            int color = Color.red(pixel);
            int monoPixel = (color > threshold) ? 1 : 0;
            pixelsMonochrome[index++] = monoPixel;
        }

        // Konversi skala abu-abu ke format bit
        byte[] imageBuffer = new byte[(width * height) / 8];
        int bufferIndex = 0; // Perbaikan: Menambahkan variabel untuk indeks buffer

        for (int pixel : pixelsMonochrome) {
            currentByte |= (pixel & 0x01) << (7 - bitIndex);
            bitIndex++;

            if (bitIndex == 8) {
                imageBuffer[bufferIndex++] = (byte) currentByte; // Perbaikan: Menggunakan bufferIndex
                currentByte = 0;
                bitIndex = 0;
            }
        }

        return imageBuffer;
    }

    // Modifikasi metode printQRCodeViaBluetooth untuk menerima BluetoothDevice
    public void printQRCodeViaBluetooth(Bitmap qrBit, BluetoothDevice bluetoothDevice) throws IOException {
        try {
            // Buka koneksi Bluetooth dengan BluetoothDevice
            openBT(bluetoothDevice);

            // Print QR Code
            printBitmap(qrBit);

            // Tutup koneksi Bluetooth
            closeBT();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
