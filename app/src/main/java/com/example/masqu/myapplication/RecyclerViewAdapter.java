package com.example.masqu.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by masqu on 2017-01-03.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ItemHolder> {
    private List<String> itemsName;
    private OnItemClickListener onItemClickListener;
    private LayoutInflater layoutInflater;
    private Context mContext;
    private int halfScreenWidth;


    public RecyclerViewAdapter(Context context, int widthPixels) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;

        itemsName = new ArrayList<String>();
        halfScreenWidth = Math.round((float) widthPixels / 2);
    }

    @Override
    public RecyclerViewAdapter.ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //View itemView = layoutInflater.inflate(R.layout.layout_item, parent, false);
        //return new ItemHolder(itemView, this);
        View itemCardView = layoutInflater.inflate(R.layout.image_view, parent, false);
        return new ItemHolder(itemCardView, this);
    }

    @Override
    public void onBindViewHolder(RecyclerViewAdapter.ItemHolder holder, int position) {
        holder.setImageSource(itemsName.get(position), halfScreenWidth);
    }

    @Override
    public int getItemCount() {
        return itemsName.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(ItemHolder item, int position);
    }

    public void add(int location, String iName) {
        itemsName.add(location, iName);
        notifyItemInserted(location);
    }

    public void remove(int location) {
        if (location >= itemsName.size())
            return;

        itemsName.remove(location);
        notifyItemRemoved(location);
    }

    public static class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private RecyclerViewAdapter parent;

        private ImageView imageView;

        /*
        public ItemHolder(View itemView, RecyclerViewAdapter parent) {
            super(itemView);
            itemView.setOnClickListener(this);
            this.parent = parent;
            imageView = (TextView) itemView.findViewById(R.id.item_name);
        }
        */

        public ItemHolder(View view, RecyclerViewAdapter parent) {
            super(view);
            imageView = (ImageView) view.findViewById(R.id.img_view);
            imageView.setOnClickListener(this);
            this.parent = parent;
        }

        public void setImageSource(String path, int halfScreenWidth) {
            Bitmap bm = decodeSampledBitmapFromUri(path, halfScreenWidth, 300, 200);
            imageView.setImageBitmap(bm);
//            imageView.setImageResource(R.drawable.ic_swap_vert_black_24dp);
        }


        @Override
        public void onClick(View v) {
            final OnItemClickListener listener = parent.getOnItemClickListener();
            if (listener != null) {
                listener.onItemClick(this, getPosition());
            }
        }
    }

    public static Bitmap decodeSampledBitmapFromUri(String path, int halfScreenWidth, int reqWidth, int reqHeight) {

        Bitmap scaledBitmap = null;
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, halfScreenWidth, reqWidth, reqHeight);


        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        scaledBitmap = BitmapFactory.decodeFile(path, options);
//        final int THUMBSIZE = 64;

//        Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path,options),
//                THUMBSIZE, THUMBSIZE);
        Bitmap rotatedBitmap = scaledBitmap;

//        if (options.outHeight < options.outWidth) {
//            Matrix matrix = new Matrix();
//
//            matrix.postRotate(90);
//
//
//            rotatedBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
//        }

        return rotatedBitmap;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int halfScreenWidth, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (width > halfScreenWidth) {
            inSampleSize = Math.round((float) width / (float) halfScreenWidth);
        } else {
            inSampleSize = Math.round((float) halfScreenWidth / (float) width);
        }
//
//        if (height > reqHeight || width > reqWidth) {
//            if (width > height) {
//                inSampleSize = Math.round((float) height / (float) reqHeight);
//            } else {
//                inSampleSize = Math.round((float) width / (float) reqWidth);
//            }
//        }

        return inSampleSize;
    }
}
