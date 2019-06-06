package com.example.boris.wallpaperslideshow.adapters;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

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
        Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap,
                320, 500);

        holder.imageView.setImageBitmap(thumbnail);
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
            itemView.setOnLongClickListener(v -> {

                PopupMenu popupMenu = new PopupMenu(mainActivity, itemView);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()){
                        case R.id.deleteImage:
                            boolean isDeleted = photoModels.get(getAdapterPosition()).getFile().delete();
                            mainActivity.makeErrorNotification("Item is deleted: " + isDeleted);
                            photoModels.remove(getAdapterPosition());
                            notifyDataSetChanged();
                            return true;
                    }
                    return false;
                });
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.recycler_menu, popupMenu.getMenu());
                popupMenu.show();
                return true;
            });
        }
    }

    public void addElem(List<PhotoModel> models){
        photoModels = models;
        notifyItemInserted(photoModels.size() - 1);
    }
}
