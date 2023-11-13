package com.example.parkirfirebase;

public class LokasiModel {
    private String namaLokasi;
    private String imageUrl;

    public LokasiModel() {
        // Diperlukan oleh Firebase untuk deserialisasi objek
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
}
