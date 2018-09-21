package com.andeqa.andeqa.camera;

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

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.chatting.MessagingActivity;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.andeqa.andeqa.creation.PreviewImagePostActivity;
import com.andeqa.andeqa.creation.PreviewVideoPostActivity;
import com.andeqa.andeqa.profile.UpdateProfileActivity;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
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

    private static final String GALLERY_PATH ="gallery image";
    private static final String GALLERY_VIDEO ="gallery video";
    private static final String POST_TAG = CreateCollectionPostActivity.class.getSimpleName();
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String PREVIEW_POST = "preview post";


    private static final int VIDEO = 0;
    private static final int IMAGE = 2;

    private String mUid;
    private String mRoomId;
    private String postIntent;
    private String collectionIntent;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;
    private String  album_name;
    private String path;


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
        postIntent = getIntent().getStringExtra(POST_TAG);
        collectionId = getIntent().getStringExtra(COLLECTION_ID);
        collectionIntent = getIntent().getStringExtra(COLLECTION_TAG);
        collectionSettingsIntent  = getIntent().getStringExtra(COLLECTION_SETTINGS_COVER);
        profileCoverIntent = getIntent().getStringExtra(PROFILE_COVER_PATH);
        profilePhotoIntent = getIntent().getStringExtra(PROFILE_PHOTO_PATH);
        mUid = getIntent().getStringExtra(EXTRA_USER_UID);
        mRoomId = getIntent().getStringExtra(EXTRA_ROOM_ID);
        setTitle(album_name);

        loadAlbumTask = new LoadAlbumImages();
        loadAlbumTask.execute();
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
            Cursor cursor = new MergeCursor(new Cursor[]{imagesCursorExternal,imagesCursorInternal});
            while (cursor.moveToNext()) {

                try {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    mediaFiles.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            cursor.close();
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
            final String path = mediaFiles.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp") || fileExtension.equals("mkv")
                    || fileExtension.equals("flv")){
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

            final String path = mediaFiles.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp") || fileExtension.equals("mkv")
                    || fileExtension.equals("flv")){
                populateVideo((GalleryVideoViewHolder) holder, position);
            }else {
                populateImage((GalleryImageViewHolder)holder, position);
            }

        }

        @Override
        public int getItemCount() {
            return mediaFiles.size();
        }

        private void populateImage(final GalleryImageViewHolder holder, final int position){

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
                    if (postIntent != null) {
                        Intent intent = new Intent(AlbumActivity.this, PreviewImagePostActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.POST_TAG, postIntent);
                        startActivity(intent);
                        finish();
                    } else if (collectionId != null) {
                        Intent intent = new Intent(AlbumActivity.this, CreateCollectionActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        finish();
                    } else if (collectionSettingsIntent != null) {
                        Intent intent = new Intent(AlbumActivity.this, CollectionSettingsActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        finish();
                    }else if (profilePhotoIntent != null){
                        Intent intent = new Intent(AlbumActivity.this, UpdateProfileActivity.class);
                        intent.putExtra(AlbumActivity.PROFILE_PHOTO_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    } else if (profileCoverIntent != null){
                        Intent intent = new Intent(AlbumActivity.this, UpdateProfileActivity.class);
                        intent.putExtra(AlbumActivity.PROFILE_COVER_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    }else if (mRoomId != null){
                        Intent intent = new Intent(AlbumActivity.this, MessagingActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(AlbumActivity.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(AlbumActivity.this, PreviewImagePostActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }

        private void populateVideo(final GalleryVideoViewHolder holder, final int position){
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
                    if (collectionId != null) {
                        Intent intent = new Intent(AlbumActivity.this, PreviewVideoPostActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_VIDEO, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(AlbumActivity.COLLECTION_ID, collectionId);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(AlbumActivity.this, PreviewVideoPostActivity.class);
                        intent.putExtra(AlbumActivity.GALLERY_VIDEO, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                    }
                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();

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
    }
}
