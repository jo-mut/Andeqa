package com.cinggl.cinggl.bitmap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.provider.MediaStore;

import com.cinggl.cinggl.adapters.ImageAdapter;


public class ImageThread implements Runnable {

    private ImageAdapter.ViewHolder holder;
    private Context context;

    public ImageThread(Context context, ImageAdapter.ViewHolder holder) {
        this.holder = holder;
        this.context = context;
    }

    @Override
    public void run() {
        final Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                    context.getContentResolver(), holder.id,
                    MediaStore.Images.Thumbnails.MINI_KIND, null);

        ((Activity) context).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                holder.imageView.setImageBitmap(bitmap);
            }
        });
    }
}
