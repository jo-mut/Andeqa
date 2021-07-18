package com.andeka.andeka.gallery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeka.andeka.R;
import com.andeka.andeka.creation.PreviewImagePostActivity;
import com.andeka.andeka.creation.PreviewVideoPostActivity;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AlbumActivity extends AppCompatActivity {

    @Bind(R.id.galleryRecyclerView)RecyclerView mPicturesGridView;
    @Bind(R.id.toolbar)Toolbar toolbar;
    ArrayList<HashMap<String, String>> mediaFiles = new ArrayList<HashMap<String, String>>();
    LoadAlbumImages loadAlbumTask;

    private static final String IMAGE_PATH ="image path";
    private static final String VIDEO_PATH = "video path";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;

    private static final int VIDEO = 0;
    private static final int IMAGE = 2;

    private String  album_name;
    private ItemOffsetDecoration itemOffsetDecoration;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        album_name = getIntent().getStringExtra("name");
        mCollectionId = getIntent().getStringExtra(COLLECTION_ID);
        postId = getIntent().getStringExtra(POST_ID);
        setTitle(album_name);

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();

        itemOffsetDecoration = new ItemOffsetDecoration(this, R.dimen.item_off_set);

    }

    class LoadAlbumImages extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mediaFiles.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String searchParams = null;
            searchParams = "bucket_display_name = \""+album_name+"\"";
            Uri imagesExternalUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri imagesInternalUri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            Uri videosInternalUri = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;
            Uri videosExternalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };

            Cursor imagesCursorExternal = getContentResolver().query(imagesExternalUri, null, searchParams,
                    null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            Cursor imagesCursorInternal = getContentResolver().query(imagesInternalUri, null, searchParams,
                    null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            Cursor videosCursorExternal = getContentResolver().query(videosInternalUri, null, searchParams,
                    null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            Cursor videosCursorInternal = getContentResolver().query(videosExternalUri, null, searchParams,
                    null, MediaStore.Images.Media.DATE_TAKEN + " DESC");
            Cursor imageCursor = new MergeCursor(new Cursor[]{imagesCursorExternal,imagesCursorInternal});
            Cursor videoCursor = new MergeCursor(new Cursor[]{videosCursorExternal, videosCursorInternal});

            while (imageCursor.moveToNext()) {

                try {
                    path = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    mediaFiles.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null, "image"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            while (videoCursor.moveToNext()) {

                try {
                    path = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    mediaFiles.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null, "video"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            imageCursor.close();
            videoCursor.close();

            Collections.sort(mediaFiles, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            AlbumActivityAdapter albumActivityAdapter = new AlbumActivityAdapter(AlbumActivity.this);
            GridLayoutManager layoutManager = new GridLayoutManager(AlbumActivity.this, 3);
            mPicturesGridView.setLayoutManager(layoutManager);
            mPicturesGridView.setHasFixedSize(false);
            mPicturesGridView.setAdapter(albumActivityAdapter);
            mPicturesGridView.addItemDecoration(itemOffsetDecoration);

        }
    }

    class AlbumActivityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Context mContext;
        private ArrayList<HashMap< String, String >> data;

        public AlbumActivityAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getItemViewType(int position) {
            final String type = mediaFiles.get(position).get(Function.KEY_TYPE);

            if (type.equals("video")){
                return VIDEO;
            }else {
                return IMAGE;
            }
        }

        @Override
        public long getItemId(int position) {
            return super.getItemId(position);
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            switch (viewType) {
                case VIDEO:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_gallery_video, parent, false);
                    return new GalleryVideoViewHolder(view);
                case IMAGE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_gallery_image, parent, false);
                    return new GalleryImageViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, final int position) {

            switch (holder.getItemViewType()) {
                case VIDEO:
                    populateVideoAlbum((GalleryVideoViewHolder) holder, position);
                case IMAGE:
                    try {
                        populateImageAlbum((GalleryImageViewHolder) holder, position);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
            }
        }

        @Override
        public int getItemCount() {
            return mediaFiles.size();
        }

        private void populateImageAlbum(final GalleryImageViewHolder holder, final int position){

            try {
                Glide.with(mContext)
                        .load(new File(mediaFiles.get(position).get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.picsImageView);

            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.picsImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCollectionId != null){
                        Intent intent = new Intent(AlbumActivity.this, PreviewImagePostActivity.class);
                        intent.putExtra(AlbumActivity.IMAGE_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.COLLECTION_ID, mCollectionId);
                        startActivity(intent);
                        finish();
                    }  else if (postId != null){
                        Intent intent = new Intent(AlbumActivity.this, PreviewImagePostActivity.class);
                        intent.putExtra(AlbumActivity.IMAGE_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.POST_ID, postId);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(AlbumActivity.this, PreviewImagePostActivity.class);
                        intent.putExtra(AlbumActivity.IMAGE_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                    }
                }
            });

        }

        private void populateVideoAlbum(final GalleryVideoViewHolder holder, final int position){
            try {
                Glide.with(mContext)
                        .load(new File(mediaFiles.get(+position).get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.videoImageView);
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.playImageView.setVisibility(View.VISIBLE);

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                     if (mCollectionId != null){
                        Intent intent = new Intent(AlbumActivity.this, PreviewVideoPostActivity.class);
                        intent.putExtra(AlbumActivity.VIDEO_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.COLLECTION_ID, mCollectionId);
                        startActivity(intent);
                        finish();
                    }else if (postId != null){
                        Intent intent = new Intent(AlbumActivity.this, PreviewVideoPostActivity.class);
                        intent.putExtra(AlbumActivity.VIDEO_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                         intent.putExtra(AlbumActivity.POST_ID, postId);
                         startActivity(intent);
                    }else {
                         Intent intent = new Intent(AlbumActivity.this, PreviewVideoPostActivity.class);
                         intent.putExtra(AlbumActivity.VIDEO_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                         startActivity(intent);
                     }
                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        mPicturesGridView.addItemDecoration(itemOffsetDecoration);

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPicturesGridView.removeItemDecoration(itemOffsetDecoration);
    }
}
