package com.example.masqu.myapplication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by masqu on 2017-01-06.
 */
public class ImageAdapter  extends RecyclerView.Adapter<ImageAdapter.ViewHolder>{

    private File imagesFile;

    public ImageAdapter(File folderFile) {
        this.imagesFile = folderFile;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.image_view,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        File imageFile = imagesFile.listFiles()[position];
        Bitmap imageBitMap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        holder.getImageView().setImageBitmap(imageBitMap);
    }

    @Override
    public int getItemCount() {
        return imagesFile.listFiles().length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            this.imageView = (ImageView) view.findViewById(R.id.img_view);
        }

        public ImageView getImageView (){
            return imageView;
        }
    }
}
