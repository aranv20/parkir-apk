package com.example.parkirfirebase.history;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.parkirfirebase.R;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<ImageModel> imageList;
    private List<ImageModel> imageListFull; // Full list without filtering

    public ImageAdapter(List<ImageModel> imageList) {
        this.imageList = imageList;
        this.imageListFull = new ArrayList<>(imageList); // Copy all data to the full list
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        ImageModel imageModel = imageList.get(position);

        // Set data to the views inside the ViewHolder
        holder.namaLokasiTextView.setText(imageModel.getNamaLokasi());
        holder.waktuTextView.setText(imageModel.getWaktu());

        // Use Glide to load the image from the URL
        Glide.with(holder.itemView.getContext())
                .load(imageModel.getImageUrl())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        // Handle the error here.
                        Log.e("Glide", "Load failed", e);
                        e.logRootCauses("Glide");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Image loaded successfully.
                        return false;
                    }
                })
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    // This method is used to update the list with search results
    public void filterList(String query) {
        // Clear the previous data to store the latest search results
        imageList.clear();

        // If the query is empty, revert to the full list
        if (query.isEmpty()) {
            imageList.addAll(imageListFull);
        } else {
            // If not empty, filter based on the query
            query = query.toLowerCase();
            for (ImageModel model : imageListFull) {
                if (model.getNamaLokasi().toLowerCase().contains(query) || model.getWaktu().toLowerCase().contains(query)) {
                    imageList.add(model);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void updateList(List<ImageModel> searchResults) {
        // Bersihkan data sebelumnya untuk menyimpan hasil pencarian terbaru
        imageList.clear();

        // Tambahkan semua item dari searchResults ke dalam imageList
        imageList.addAll(searchResults);

        // Beritahu adapter bahwa kumpulan data telah berubah
        notifyDataSetChanged();
    }


    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView namaLokasiTextView;
        TextView waktuTextView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            namaLokasiTextView = itemView.findViewById(R.id.namaLokasi);
            waktuTextView = itemView.findViewById(R.id.waktu);
        }
    }
}
