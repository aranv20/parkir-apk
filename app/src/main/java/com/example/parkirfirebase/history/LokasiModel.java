package com.example.parkirfirebase.history;

import java.util.Date;

public class LokasiModel {
    private String namaLokasi;
    private String imageUrl;
    private Date waktu; // Tambahkan atribut waktu

    public LokasiModel(String namaLokasi, String imageUrl, Date waktu) {
        this.namaLokasi = namaLokasi;
        this.imageUrl = imageUrl;
        this.waktu = waktu;
    }

    public LokasiModel() {
        // Diperlukan konstruktor kosong untuk deserialisasi Firestore
    }

    public LokasiModel(String namaLokasi, String imageUrl) {
        this.namaLokasi = namaLokasi;
        this.imageUrl = imageUrl;
    }

    public String getNamaLokasi() {
        return namaLokasi;
    }

    public void setNamaLokasi(String namaLokasi) {
        this.namaLokasi = namaLokasi;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Date getWaktu() {
        return waktu;
    }

    public void setWaktu(Date waktu) {
        this.waktu = waktu;
    }
}
