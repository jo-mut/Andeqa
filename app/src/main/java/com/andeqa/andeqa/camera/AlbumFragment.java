package com.andeqa.andeqa.camera;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class AlbumFragment extends Fragment {
    static final int REQUEST_PERMISSION_KEY = 1;
    LoadAlbum loadAlbumTask;
    @Bind(R.id.albumRecyclerView)RecyclerView albumRecyclerView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();

    private static final String GALLERY_PATH ="gallery image";
    private static final String POST_TAG = CreateCollectionPostActivity.class.getSimpleName();
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";

    private String mUid;
    private String mRoomId;
    private String postIntent;
    private String collectionIntent;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;

    private static final int VIDEO = 0;
    private static final int IMAGE = 2;



    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);

        postIntent = getActivity().getIntent().getStringExtra(POST_TAG);
        collectionId = getActivity().getIntent().getStringExtra(COLLECTION_ID);
        collectionIntent = getActivity().getIntent().getStringExtra(COLLECTION_TAG);
        collectionSettingsIntent  = getActivity().getIntent().getStringExtra(COLLECTION_SETTINGS_COVER);
        profileCoverIntent = getActivity().getIntent().getStringExtra(PROFILE_COVER_PATH);
        profilePhotoIntent = getActivity().getIntent().getStringExtra(PROFILE_PHOTO_PATH);
        mUid = getActivity().getIntent().getStringExtra(EXTRA_USER_UID);
        mRoomId = getActivity().getIntent().getStringExtra(EXTRA_ROOM_ID);

        return view;
    }

    class LoadAlbum extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            albumList.clear();
        }

        protected String doInBackground(String... args) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            String countPhoto = null;
            Uri imagesExternalUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri imagesInternalUri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            Uri videosInternalUri = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;
            Uri videosExternalUri = android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI;


            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };
            Cursor imageCursorExternal = getActivity().getApplicationContext().getContentResolver()
                    .query(imagesExternalUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor imageCursorInternal = getActivity().getApplicationContext().getContentResolver()
                    .query(imagesInternalUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                    null, null);
            Cursor videoCursorExternal = getActivity().getApplicationContext().getContentResolver()
                    .query(videosExternalUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                            null, null);
            Cursor videoCursorInternal = getActivity().getApplicationContext().getContentResolver()
                    .query(videosInternalUri, projection, "_data IS NOT NULL) GROUP BY (bucket_display_name",
                            null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{imageCursorExternal,imageCursorInternal});

            while (cursor.moveToNext()) {

                try {
                    path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    countPhoto = Function.getImagesCount(getActivity().getApplicationContext(), album);
                    albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            cursor.close();
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            AlbumsAdapter albumsAdapter = new AlbumsAdapter(getContext());
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 2);
            albumRecyclerView.setLayoutManager(layoutManager);
            albumRecyclerView.setHasFixedSize(false);
            albumRecyclerView.setAdapter(albumsAdapter);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case REQUEST_PERMISSION_KEY: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    loadAlbumTask = new LoadAlbum();
                    loadAlbumTask.execute();
                } else
                {
                    Toast.makeText(getContext(), "You must accept permissions.", Toast.LENGTH_LONG).show();
                }
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();

        String[] PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if(!Function.hasPermissions(getContext(), PERMISSIONS)){
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, REQUEST_PERMISSION_KEY);
        }else{
            loadAlbumTask = new LoadAlbum();
            loadAlbumTask.execute();
        }

    }

    class AlbumsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Context mContext;
        private Player player;
        private ArrayList<HashMap< String, String >> data;

        public AlbumsAdapter(Context mContext) {
            this.mContext = mContext;
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
                            .inflate(R.layout.layout_video_album, parent, false);
                    return new VideoAlbumViewHolder(view);
                case IMAGE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_image_album, parent, false);
                    return new ImageAlbumViewHolder(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            final String path = albumList.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp")){
                return VIDEO;
            }else {
                return IMAGE;
            }
        }


        @Override
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, final int position) {

            final String path = albumList.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp")){
                populateVideoAlbum((VideoAlbumViewHolder) holder, position);
            }else {
                populateImageAlbum((ImageAlbumViewHolder) holder, position);
            }

        }

        @Override
        public int getItemCount() {
            return albumList.size();
        }

        private void populateImageAlbum(final ImageAlbumViewHolder holder, final int position){
            try {
                Glide.with(mContext)
                        .load(new File(albumList.get(+position).get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.albumImageView);
                holder.galleryTitleTextView.setText(albumList.get(position).get(Function.KEY_ALBUM));
                holder.galleryCountTextView.setText(albumList.get(position).get(Function.KEY_COUNT));

            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.albumLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_ID, collectionId);
                        intent.putExtra(AlbumFragment.POST_TAG, CreateCollectionPostActivity.class.getSimpleName());
                    } else if (collectionIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_TAG, CreateCollectionActivity.class.getSimpleName());
                        startActivity(intent);
                    } else if (collectionSettingsIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_SETTINGS_COVER, CollectionSettingsActivity.class.getSimpleName());
                        intent.putExtra(AlbumFragment.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (profilePhotoIntent != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.PROFILE_PHOTO_PATH, "profile photo path");
                        startActivity(intent);
                        getActivity().finish();
                    } else if (profileCoverIntent != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.PROFILE_COVER_PATH, "profile cover path");
                        startActivity(intent);
                        getActivity().finish();
                    }else if (mRoomId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(AlbumFragment.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        getActivity().finish();
                    }else {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        startActivity(intent);
                    }
                }
            });
        }

        private void populateVideoAlbum(final VideoAlbumViewHolder holder, final int position){
            try {
                Glide.with(mContext)
                        .load(new File(albumList.get(+position).get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.videoImageView);
                holder.galleryTitleTextView.setText(albumList.get(position).get(Function.KEY_ALBUM));
                holder.galleryCountTextView.setText(albumList.get(position).get(Function.KEY_COUNT));
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (postIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_ID, collectionId);
                        startActivity(intent);
                    } else if (collectionIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_TAG, CreateCollectionActivity.class.getSimpleName());
                        startActivity(intent);
                    } else if (collectionSettingsIntent != null) {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_SETTINGS_COVER, CollectionSettingsActivity.class.getSimpleName());
                        intent.putExtra(AlbumFragment.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (profilePhotoIntent != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.PROFILE_PHOTO_PATH, "profile photo path");
                        startActivity(intent);
                        getActivity().finish();
                    } else if (profileCoverIntent != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.PROFILE_COVER_PATH, "profile cover path");
                        startActivity(intent);
                        getActivity().finish();
                    }else if (mRoomId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(AlbumFragment.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        getActivity().finish();
                    }else {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        startActivity(intent);
                        getActivity().finish();
                    }
                }
            });
        }
    }


}
