package com.cinggl.cinggl.camera;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.Toast;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.adapters.ImageAdapter;
import com.cinggl.cinggl.bitmap.BitmapWorker;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleCreateFragment extends Fragment {

    public static ArrayList<String> selected_urls;
    private ImageAdapter adapter;
    private View loading;
    private int core;
    public static ExecutorService executor;
    public static final String EXTRA_PATH = "e1";
    public static final String EXTRA_NUM = "e2";

    ImageAdapter.OnClickItemListener onClickThumbnail = new ImageAdapter.OnClickItemListener() {
        @Override
        public void onClick(final String url) {
//            SliceDialog.newInstance(new SliceDialog.SelectListener() {
//                @Override
//                public void onSelect(int total, int scale) {
//                    startSlicing(url, total, scale);
//                }
//            }).show(getFragmentManager(), "");
        }
    };



    public CingleCreateFragment() {
            // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_cingle_create, container, false);

        final GridView gridview = (GridView) fragment.findViewById(R.id.gridview);
//        loading = getActivity().findViewById(R.id.loading);

        core = Runtime.getRuntime().availableProcessors();
        executor = new ThreadPoolExecutor(
                core + 1,
                core * 2 + 1,
                60l,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>()
        );

        new Thread() {
            @Override
            public void run() {
                try {

                    long[] thumbnails;
                    String[] urls;

                    Cursor c = getActivity().getContentResolver().query(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                            new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA },
                            null, null, null
                    );

                    if (c == null) return;

                    final int length = c.getCount();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (length == 0) {
                                getActivity().findViewById(R.id.empty).setVisibility(View.VISIBLE);
                            } else {
//                                getActivity().findViewById(R.id.empty).setVisibility(View.GONE);
                            }
                        }
                    });

                    if (length == 0) return;

                    thumbnails = new long[length];
                    urls = new String[length];
                    selected_urls = new ArrayList<>();

                    int i = 0;
                    while (c.moveToNext()) {
                        urls[i] = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA));
                        thumbnails[i] = c.getLong(c.getColumnIndex(MediaStore.Images.Media._ID));
                        i++;
                    }

                    c.close();

                    adapter = new ImageAdapter(getActivity(), thumbnails, urls, onClickThumbnail);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gridview.setAdapter(adapter);
                        }
                    });
                } finally {
                    interrupt();
                }
            }
        }.start();

        return fragment;
    }

//    public void back() {
//        if (loading.getVisibility() == View.VISIBLE) return;
//
//        getActivity().finish();
//    }

//    private void startSlicing(String path, final int total, int scale) {
//        loading.setVisibility(View.VISIBLE);
//
//        BitmapWorker.slice(getActivity().getApplicationContext(), scale, path, total, core, new BitmapWorker.CompleteListener() {
//            @Override
//            public void onSave(final String path) {
//                getActivity().runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        loading.setVisibility(View.GONE);
//                        if (path != null) {
//                            CompleteDialog.newInstance(path, new CompleteDialog.ViewListener() {
//                                @Override
//                                public void onView(String path) {
//                                    Intent intent = new Intent(getActivity().getApplicationContext(), ViewActivity.class);
//                                    intent.putExtra(EXTRA_PATH, path);
//                                    intent.putExtra(EXTRA_NUM, total);
//                                    startActivity(intent);
//                                }
//                            }).show(getFragmentManager(), "");
//                        } else {
//                            Toast.makeText(getActivity().getApplicationContext(), R.string.deleted, Toast.LENGTH_LONG).show();
//                        }
//                    }
//                });
//            }
//        });
//    }


}

