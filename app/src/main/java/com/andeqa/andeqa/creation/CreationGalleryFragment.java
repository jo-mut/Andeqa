package com.andeqa.andeqa.creation;


import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.utils.Function;
import com.andeqa.andeqa.utils.MapComparator;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.os.Build.VERSION_CODES.M;

/**
 * A simple {@link Fragment} subclass.
 */
public class CreationGalleryFragment extends Fragment {

    @Bind(R.id.galleryRecyclerView)
    RecyclerView mGalleryRecyclerView;
    @Bind(R.id.postImageView)ImageView mPostImageView;


    private List<HashMap<String, String>> photos = new ArrayList<HashMap<String, String>>();
    private LoadPhotos loadPhotos;
    private GalleryAdapter galleryAdapter;
    private GridLayoutManager gridLayoutManager;

    public CreationGalleryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_creation_gallery, container, false);
        ButterKnife.bind(this, view);

        loadPhotos = new LoadPhotos();
        loadPhotos.execute();

        return view;

    }

    class LoadPhotos extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            photos.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            Uri uriExternal = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri uriInternal = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };

            Cursor cursorExternal = getActivity().getContentResolver().query(uriExternal, projection, null, null, null);
            Cursor cursorInternal = getActivity().getContentResolver().query(uriInternal, projection, null, null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                photos.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(photos, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));
            //Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            galleryAdapter = new GalleryAdapter(getActivity(), photos);
            gridLayoutManager = new GridLayoutManager(getContext(), 3);
            mGalleryRecyclerView.setAdapter(galleryAdapter);
            mGalleryRecyclerView.setNestedScrollingEnabled(false);
            mGalleryRecyclerView.setLayoutManager(gridLayoutManager);

            try {
                Glide.with(getContext())
                        .load(new File(photos.get(0).get(Function.KEY_PATH)))
                        .into(mPostImageView);
            }catch (Exception e){}

        }

    }


    class GalleryAdapter extends RecyclerView.Adapter<PicsViewHolder>{
        private Activity activity;
        List<HashMap<String, String>> photos = new ArrayList<HashMap<String, String>>();

        public GalleryAdapter(Activity activity, List<HashMap<String, String>> photos) {
            this.activity = activity;
            this.photos = photos;
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @Override
        public PicsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.picture_layout, parent, false);
            return  new PicsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final PicsViewHolder holder, final int position) {
            holder.picsImageView.setId(position);

            HashMap<String, String> pics = new HashMap<>();
            pics = photos.get(position);

            holder.picsImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        Glide.with(getContext())
                                .load(new File(photos.get(position).get(Function.KEY_PATH)))
                                .into(mPostImageView);
                    }catch (Exception e){}
                }
            });
            try {
                Glide.with(getContext())
                        .load(new File(pics.get(Function.KEY_PATH)))
                        .into(holder.picsImageView);
            }catch (Exception e){}
        }

    }

}
