package com.andeqa.andeqa.gallery;


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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.PreviewImagePostActivity;
import com.andeqa.andeqa.player.Player;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.andeqa.andeqa.utils.ItemOffsetDecoration;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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
    LoadAlbum loadAlbumTask;
    @Bind(R.id.albumRecyclerView)RecyclerView albumRecyclerView;
    ArrayList<HashMap<String, String>> albumList = new ArrayList<HashMap<String, String>>();

    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String postId;

    private static final int VIDEO = 0;
    private static final int IMAGE = 1;

    private ItemOffsetDecoration itemOffsetDecoration;



    public AlbumFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        ButterKnife.bind(this, view);

        mCollectionId = getActivity().getIntent().getStringExtra(COLLECTION_ID);
        postId = getActivity().getIntent().getStringExtra(POST_ID);

        itemOffsetDecoration = new ItemOffsetDecoration(getContext(), R.dimen.item_off_set);
        loadAlbumTask = new LoadAlbum();
        loadAlbumTask.execute();
        albumRecyclerView.addItemDecoration(itemOffsetDecoration);

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
            Cursor imageCursor = new MergeCursor(new Cursor[]{imageCursorExternal,imageCursorInternal});
            Cursor videoCursor = new MergeCursor(new Cursor[]{videoCursorExternal, videoCursorInternal});

            while (imageCursor.moveToNext()) {

                try {
                    path = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = imageCursor.getString(imageCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    countPhoto = Function.getImagesCount(getActivity().getApplicationContext(), album);
                    albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto, "image"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            while (videoCursor.moveToNext()) {

                try {
                    path = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                    album = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                    timestamp = videoCursor.getString(videoCursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));
                    countPhoto = Function.getImagesCount(getActivity().getApplicationContext(), album);
                    albumList.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), countPhoto, "video"));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            imageCursor.close();
            videoCursor.close();
            // Arranging photo album by timestamp decending
            Collections.sort(albumList, new MapComparator(Function.KEY_TIMESTAMP, "dsc"));
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            AlbumsAdapter albumsAdapter = new AlbumsAdapter(getContext());
            GridLayoutManager layoutManager = new GridLayoutManager(getContext(), 3);
            albumRecyclerView.setLayoutManager(layoutManager);
            albumRecyclerView.setHasFixedSize(false);
            albumRecyclerView.setAdapter(albumsAdapter);
            albumRecyclerView.addItemDecoration(itemOffsetDecoration);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        super.onPause();
        albumRecyclerView.removeItemDecoration(itemOffsetDecoration);
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
                            .inflate(R.layout.layout_gallery_album, parent, false);
                    return new ImageAlbumViewHolder(view);
                case IMAGE:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.layout_gallery_album, parent, false);
                    return new ImageAlbumViewHolder(view);
            }
            return null;
        }

        @Override
        public int getItemViewType(int position) {
            final String type = albumList.get(position).get(Function.KEY_TYPE);

            if (type.equals("video")){
                return VIDEO;
            }else {
                return IMAGE;
            }
        }


        @Override
        public void onBindViewHolder(final @NonNull RecyclerView.ViewHolder holder, final int position) {

            switch (holder.getItemViewType()) {
                case VIDEO:
                    populateVideoAlbum((ImageAlbumViewHolder) holder, position);
                case IMAGE:
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
                        .load(new File(albumList.get(+position).get(Function.KEY_PATH)))
                        .apply(new RequestOptions()
                                .override(400, 400)
                                .centerCrop())// Uri of the picture
                        .into(holder.albumImageView);
                holder.galleryTitleTextView.setText(albumList.get(position).get(Function.KEY_ALBUM));

            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.albumLinearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCollectionId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_ID, mCollectionId);
                        startActivity(intent);
                    } else if (postId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.POST_ID, postId);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        startActivity(intent);
                    }
                }
            });
        }

        private void populateVideoAlbum(final ImageAlbumViewHolder holder, final int position){
            try {
                Glide.with(mContext)
                        .load(new File(albumList.get(+position).get(Function.KEY_PATH))) // Uri of the picture
                        .into(holder.albumImageView);
                holder.galleryTitleTextView.setText(albumList.get(position).get(Function.KEY_ALBUM));
            } catch (Exception e) {
                e.printStackTrace();
            }

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCollectionId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.COLLECTION_ID, mCollectionId);
                        startActivity(intent);
                    }else if (postId != null){
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        intent.putExtra(AlbumFragment.POST_ID, postId);
                        startActivity(intent);
                    }else {
                        Intent intent = new Intent(getActivity(), AlbumActivity.class);
                        intent.putExtra("name", albumList.get(position).get(Function.KEY_ALBUM));
                        startActivity(intent);
                    }
                }
            });
        }
    }


}
