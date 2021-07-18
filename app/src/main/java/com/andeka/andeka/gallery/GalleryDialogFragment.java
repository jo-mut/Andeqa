package com.andeka.andeka.gallery;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.andeka.andeka.R;
import com.andeka.andeka.chatting.ChatActivity;
import com.andeka.andeka.creation.CreatePostActivity;
import com.andeka.andeka.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;


public class GalleryDialogFragment extends BottomSheetDialogFragment {
    @Bind(R.id.galleryRecyclerView)RecyclerView mGalleryRecyclerView;
    private ArrayList<HashMap<String, String>> mediaFiles = new ArrayList<HashMap<String, String>>();
    private LoadPhotos loadPhotos;
    private static final String GALLERY_PATH ="gallery image";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_POST_ID = "post id";
    private String mUid;
    private String mRoomId;
    private String mPostId;
    private ItemOffsetDecoration itemOffsetDecoration;

    public GalleryDialogFragment() {
        // Required empty public constructor
    }


    public static GalleryDialogFragment newInstance() {
        GalleryDialogFragment fragment = new GalleryDialogFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(GalleryDialogFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_gallery_dialog, container, false);
        ButterKnife.bind(this, view);

        loadPhotos = new LoadPhotos();
        loadPhotos.execute();

        Bundle bundle = getArguments();
        if (bundle != null){
            mPostId = bundle.getString(EXTRA_POST_ID);
            mUid = bundle.getString(EXTRA_USER_UID);
            mRoomId = bundle.getString(EXTRA_ROOM_ID);
        }

        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getDialog().setCanceledOnTouchOutside(true);

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

                    mediaFiles.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null, "image"));

                }catch (Exception e){
                    e.printStackTrace();
                }

            }
            cursor.close();
            Collections.sort(mediaFiles, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));
            // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            PhotosAdapter photosAdapter = new PhotosAdapter(getContext());
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            mGalleryRecyclerView.setLayoutManager(layoutManager);
            mGalleryRecyclerView.setHasFixedSize(false);
            mGalleryRecyclerView.setAdapter(photosAdapter);
            mGalleryRecyclerView.addItemDecoration(itemOffsetDecoration);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mGalleryRecyclerView.addItemDecoration(itemOffsetDecoration);
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGalleryRecyclerView.removeItemDecoration(itemOffsetDecoration);
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

            if (mRoomId != null){
                holder.picsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(GalleryDialogFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryDialogFragment.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(GalleryDialogFragment.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }

            if (mPostId != null){
                holder.picsImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                        intent.putExtra(GalleryDialogFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryDialogFragment.EXTRA_POST_ID, mPostId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }
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
            if (mRoomId != null){
                holder.playImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), ChatActivity.class);
                        intent.putExtra(GalleryDialogFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryDialogFragment.EXTRA_ROOM_ID, mRoomId);
                        intent.putExtra(GalleryDialogFragment.EXTRA_USER_UID, mUid);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }

            if (mPostId != null){
                holder.playImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CreatePostActivity.class);
                        intent.putExtra(GalleryDialogFragment.GALLERY_PATH, mediaFiles.get(position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryDialogFragment.EXTRA_POST_ID, mPostId);
                        startActivity(intent);
                        getActivity().finish();
                    }
                });
            }
        }

    }

}
