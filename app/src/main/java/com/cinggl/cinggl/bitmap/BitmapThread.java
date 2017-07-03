package com.cinggl.cinggl.bitmap;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Rect;

import java.io.IOException;

public class BitmapThread implements Runnable {

    private int slice;
    private String filename;
    private String path;
    private BitmapFactory.Options bitmapOptions;
    private int width;
    private int height;
    private int scale;
    private Context context;

    public BitmapThread(Context context, int scale, String path, int slice, String filename, BitmapFactory.Options bitmapOptions, int width, int height) {
        this.context = context;
        this.path = path;
        this.filename = filename;
        this.slice = slice;
        this.bitmapOptions = bitmapOptions;
        this.width = width;
        this.height = height;
        this.scale = scale;
    }

    @Override
    public void run() {
        try {
            BitmapWorker.save(
                    context,
                    scale,
                    filename,
                    slice,
                    BitmapRegionDecoder.newInstance(path, false).decodeRegion(new Rect(0, height * slice, width, height * (slice + 1)), bitmapOptions)
            );
            BitmapWorker.checkParts(filename);
        } catch (IOException ignored) {}
    }
}
