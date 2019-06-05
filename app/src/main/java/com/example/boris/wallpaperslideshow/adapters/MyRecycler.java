package com.example.boris.wallpaperslideshow.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.boris.wallpaperslideshow.MainActivity;
import com.example.boris.wallpaperslideshow.R;
import com.example.boris.wallpaperslideshow.models.PhotoModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MyRecycler extends RecyclerView.Adapter<MyRecycler.MyHolder> {

    private List<PhotoModel> photoModels;
    private MainActivity mainActivity;

    public MyRecycler(List<PhotoModel> photoModels, MainActivity mainActivity) {
        this.photoModels = photoModels;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_item, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        Bitmap bitmap = null;
        try {
            FileInputStream streamIn = new FileInputStream(photoModels.get(position).getFile());
            bitmap = BitmapFactory.decodeStream(streamIn); //This gets the image
            streamIn.close();
        } catch (IOException e) {
            mainActivity.makeErrorNotification(e.toString() + "\nin recycler");
        }

        holder.imageView.setImageBitmap(bitmap);
    }

    @Override
    public int getItemCount() {
        return photoModels.size();
    }

    class MyHolder extends RecyclerView.ViewHolder{

        ImageView imageView;

        public MyHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.recyclerImage);
        }
    }
}
