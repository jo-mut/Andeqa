package com.andeqa.andeqa.camera;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.andeqa.andeqa.creation.PreviewImagePostActivity;
import com.andeqa.andeqa.creation.PreviewVideoPostActivity;
import com.andeqa.andeqa.message.MessagesAccountActivity;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.profile.UpdateProfileActivity;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.util.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class PicturesFragment extends Fragment {
    @Bind(R.id.galleryRecyclerView)RecyclerView mGalleryRecyclerView;
    private ArrayList<HashMap<String, String>> mediaFiles = new ArrayList<HashMap<String, String>>();
    private LoadPhotos loadPhotos;
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

    private String mUid;
    private String mRoomId;
    private String postIntent;
    private String collectionIntent;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;



    public PicturesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_pictures, container, false);
        ButterKnife.bind(this, view);

        loadPhotos = new LoadPhotos();
        loadPhotos.execute();

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

    class LoadPhotos extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mediaFiles.clear();
        }

        @Override
        protected String doInBackground(String... strings) {
            String xml = "";

            String path = null;
            String album = null;
            String timestamp = null;
            Uri imagesExternalUri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri imagesInternalUri = android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI;
            Uri videosInternalUri = android.provider.MediaStore.Video.Media.INTERNAL_CONTENT_URI;
            Uri videosExternalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;


            String[] projection = { MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.MediaColumns.DATE_MODIFIED };

            Cursor imagesCursorExternal = getActivity().getContentResolver().query(imagesExternalUri, projection, null, null, null);
            Cursor imagesCursorInternal = getActivity().getContentResolver().query(imagesInternalUri, projection, null, null, null);
            Cursor videosCursorExternal = getActivity().getContentResolver().query(videosInternalUri, projection, null, null, null);
            Cursor videosCursorInternal = getActivity().getContentResolver().query(videosExternalUri, projection, null, null, null);
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
            PhotosAdapter photosAdapter = new PhotosAdapter(getContext());
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            mGalleryRecyclerView.setLayoutManager(layoutManager);
            mGalleryRecyclerView.setHasFixedSize(false);
            mGalleryRecyclerView.setAdapter(photosAdapter);
        }

    }

    class PhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
        private Context mContext;
        private ArrayList<HashMap< String, String >> data;
        private static final int VIDEO = 0;
        private static final int IMAGE = 2;

        public PhotosAdapter(Context mContext) {
            this.mContext = mContext;
        }

        @Override
        public int getItemViewType(int position) {
            final String path = mediaFiles.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp") || fileExtension.equals("mkv")
                    || fileExtension.equals("flv") || fileExtension.equals("MKV") || fileExtension.equals("MP4")){
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
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder
                                                     holder, final int position) {
            final String path = mediaFiles.get(position).get(Function.KEY_PATH);
            final String fileExtension = path.substring(path.lastIndexOf(".") + 1);

            if (fileExtension.equals("mp4") || fileExtension.equals("3gp") || fileExtension.equals("mkv")
                    || fileExtension.equals("flv") || fileExtension.equals("MKV") || fileExtension.equals("MP4")){
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
                        Intent intent = new Intent(getActivity(), PreviewImagePostActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(PicturesFragment.COLLECTION_ID, collectionId);
                        intent.putExtra(PicturesFragment.POST_TAG, postIntent);
                        startActivity(intent);
                        getActivity().finish();
                    } else if (collectionIntent != null) {
                        Intent intent = new Intent(getActivity(), CreateCollectionActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        getActivity().finish();
                    } else if (collectionSettingsIntent != null) {
                        Intent intent = new Intent(getActivity(), CollectionSettingsActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(PicturesFragment.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        getActivity().finish();
                    }else if (profilePhotoIntent != null){
                        Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
                        intent.putExtra(PicturesFragment.PROFILE_PHOTO_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        getActivity().finish();
                    } else if (profileCoverIntent != null){
                        Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
                        intent.putExtra(PicturesFragment.PROFILE_COVER_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        getActivity().finish();
                    }else if (mRoomId != null){
                        Intent intent = new Intent(getActivity(), MessagesAccountActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(PicturesFragment.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(PicturesFragment.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        getActivity().finish();
                    }else {
                        Intent intent = new Intent(getActivity(), PreviewImagePostActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        startActivity(intent);
                        getActivity().finish();
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
                    if (postIntent != null) {
                        Intent intent = new Intent(getActivity(), PreviewVideoPostActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_VIDEO, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(PicturesFragment.COLLECTION_ID, collectionId);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(getActivity(), PreviewVideoPostActivity.class);
                        intent.putExtra(PicturesFragment.GALLERY_VIDEO, mediaFiles.get(position).get(Function.KEY_PATH));
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
