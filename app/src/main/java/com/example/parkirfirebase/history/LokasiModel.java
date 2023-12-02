package com.example.parkirfirebase.history;

public class LokasiModel {
    private String namaLokasi;
    private String imageUrl;

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
