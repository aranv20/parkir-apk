package com.example.parkirfirebase.history;

public class ImageModel {
    private String imageUrl;
    private String namaLokasi;
    private String waktu; // Tambahkan field waktu

    public ImageModel(String imageUrl, String namaLokasi, String waktu) {
        this.imageUrl = imageUrl;
        this.namaLokasi = namaLokasi;
        this.waktu = waktu;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNamaLokasi() {
        return namaLokasi;
    }

    public void setNamaLokasi(String namaLokasi) {
        this.namaLokasi = namaLokasi;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }
}