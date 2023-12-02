package com.example.parkirfirebase.history;

public class ImageModel {
    private String imageUrl;
    private String namaLokasi;

    public ImageModel(String imageUrl, String namaLokasi) {
        this.imageUrl = imageUrl;
        this.namaLokasi = namaLokasi;
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
}
