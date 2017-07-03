package com.cinggl.cinggl.bitmap;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BitmapWorker {

    private static int slice_counter = 0;
    private static int total_parts = 0;
    private static CompleteListener cListener;

    public static void save(Context context, int scale, String filename, int slice, Bitmap bitmap) {
        File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                .getAbsolutePath(), filename + "/" + (slice + 1) + ".png");
//        File image = new File(Environment.getExternalStorageDirectory()
//                .getAbsolutePath(), filename + "/" + (scale + 1) + "png");

        try {
            //noinspection ResultOfMethodCallIgnored
            image.mkdirs();
        } catch (Exception ignored) {}

        if (image.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                image.delete();
            } catch (Exception ignored) {}
        }

        try {
            //noinspection ResultOfMethodCallIgnored
            image.createNewFile();
        } catch (IOException e) {
            return;
        }

        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(image);
            bitmap.setHasAlpha(true);

            if (scale < 100) {
                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, (int)
                        (bitmap.getWidth() * ((float) scale / 100)),
                        (int) (bitmap.getHeight() * ((float) scale / 100)), true);
                bitmap.recycle();

                scaled.setHasAlpha(true);

                scaled.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                scaled.recycle();
            } else {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream);
                bitmap.recycle();
            }

            sendRefreshBroadcast(context, image);
            outStream.flush();
        } catch (IOException ignored) {
        } finally {
            try {
                if (outStream != null) {
                    outStream.close();
                }
            } catch (IOException ignored) {
            }
        }
    }

    private static void sendRefreshBroadcast(Context context, File file) {
        MediaScannerConnection.scanFile(context, new String[]{file.getPath()}, new String[]{"image/jpeg"}, null);
    }

    private static float free(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass() - 10;
        if (memoryClass < 1) memoryClass = 1;
        return (float) memoryClass;
    }

    public static void slice(final Context context, final int scale, final String image_url, final int total, final int core, CompleteListener listener) {
        cListener = listener;
        slice_counter = 0;
        total_parts = total;

        if (!new File(image_url).exists()) {
            checkParts(null, true);
            return;
        }

        new Thread() {
            @Override
            public void run() {
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();

                bitmapOptions.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(image_url, bitmapOptions);

                float outWidth = bitmapOptions.outWidth;
                float outHeight = bitmapOptions.outHeight;

                final float imageSize = outWidth * outHeight * 4.0f / 1024.0f / 1024.0f;

                int inSampleSize = (int) Math.pow(2, Math.floor(imageSize / free(context)));

                int newWidth = (int) Math.floor(outWidth / inSampleSize);
                int newHeight = (int) Math.floor(outHeight / (inSampleSize * total));

                bitmapOptions.inSampleSize = inSampleSize;
                bitmapOptions.inJustDecodeBounds = false;

                String filename = image_url.substring(image_url.lastIndexOf("/") + 1, image_url.lastIndexOf(".")).replace(".", "");

                ExecutorService executor = new ThreadPoolExecutor(
                        core + 1,
                        core * 2 + 1,
                        60l,
                        TimeUnit.SECONDS,
                        new LinkedBlockingQueue<Runnable>()
                );

                for (int slice = 0; slice < total; slice++) {
                    executor.execute(new BitmapThread(context, scale, image_url, slice, filename, bitmapOptions, newWidth, newHeight));
                }

                executor.shutdown();
                interrupt();
            }
        }.start();
    }

    synchronized public static void checkParts(String filename) {
        checkParts(filename, false);
    }

    synchronized public static void checkParts(String filename, boolean force) {
        if (force) {
            cListener.onSave(null);
            return;
        }
        if (slice_counter < total_parts - 1) {
            slice_counter++;
        } else {
            cListener.onSave(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), filename).getAbsolutePath());
        }
    }

    public interface CompleteListener {
        public void onSave(String path);
    }

}
