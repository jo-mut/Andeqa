package com.andeqa.andeqa.camera;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionActivity;
import com.andeqa.andeqa.creation.CreatePostActivity;
import com.andeqa.andeqa.home.MainActivity;
import com.andeqa.andeqa.profile.UpdateProfileActivity;
import com.andeqa.andeqa.settings.CollectionSettingsActivity;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class GalleryActivity extends AppCompatActivity {
    @Bind(R.id.galleryGridView)GridView mGalleryGridView;
    @Bind(R.id.toolbar) Toolbar toolbar;
    private ArrayList<HashMap<String, String>> photos = new ArrayList<HashMap<String, String>>();
    private LoadPhotos loadPhotos;
    private static final String GALLERY_PATH ="gallery image";
    private static final String POST_TAG = CreatePostActivity.class.getSimpleName();
    private static final String COLLECTION_TAG = CreateCollectionActivity.class.getSimpleName();
    private static final String COLLECTION_SETTINGS_COVER = CollectionSettingsActivity.class.getSimpleName();
    private static final String PROFILE_PHOTO_PATH = "profile photo path";
    private static final String PROFILE_COVER_PATH = "profile cover path";
    private static final String COLLECTION_ID = "collection id";


    private String postIntent;
    private String collectionIntent;
    private String collectionId;
    private String profileCoverIntent;
    private String profilePhotoIntent;
    private String collectionSettingsIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        //bind views
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_black);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });



        loadPhotos = new LoadPhotos();
        loadPhotos.execute();

        postIntent = getIntent().getStringExtra(POST_TAG);
        collectionId = getIntent().getStringExtra(COLLECTION_ID);
        collectionIntent = getIntent().getStringExtra(COLLECTION_TAG);
        collectionSettingsIntent  = getIntent().getStringExtra(COLLECTION_SETTINGS_COVER);
        profileCoverIntent = getIntent().getStringExtra(PROFILE_COVER_PATH);
        profilePhotoIntent = getIntent().getStringExtra(PROFILE_PHOTO_PATH);





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

            Cursor cursorExternal = getContentResolver().query(uriExternal, projection, null, null, null);
            Cursor cursorInternal = getContentResolver().query(uriInternal, projection, null, null, null);
            Cursor cursor = new MergeCursor(new Cursor[]{cursorExternal,cursorInternal});
            while (cursor.moveToNext()) {

                path = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME));
                timestamp = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

                photos.add(Function.mappingInbox(album, path, timestamp, Function.converToTime(timestamp), null));
            }
            cursor.close();
            Collections.sort(photos, new MapComparator(Function.KEY_TIMESTAMP, "dsc")); // Arranging photo album by timestamp decending
            return xml;
        }

        @Override
        protected void onPostExecute(String xml) {
            PhotosAdapter photosAdapter = new PhotosAdapter(GalleryActivity.this, photos);
            mGalleryGridView.setAdapter(photosAdapter);
            mGalleryGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view,
                                        final int position, long id) {
                    if (postIntent != null) {
                        Intent intent = new Intent(GalleryActivity.this, CreatePostActivity.class);
                        intent.putExtra(GalleryActivity.GALLERY_PATH, photos.get(+position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryActivity.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        finish();
                    } else if (collectionIntent != null) {
                        Intent intent = new Intent(GalleryActivity.this, CreateCollectionActivity.class);
                        intent.putExtra(GalleryActivity.GALLERY_PATH, photos.get(+position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    } else if (collectionSettingsIntent != null) {
                        Intent intent = new Intent(GalleryActivity.this, CollectionSettingsActivity.class);
                        intent.putExtra(GalleryActivity.GALLERY_PATH, photos.get(+position).get(Function.KEY_PATH));
                        intent.putExtra(GalleryActivity.COLLECTION_ID, collectionId);
                        startActivity(intent);
                        finish();
                    }else if (profilePhotoIntent != null){
                        Intent intent = new Intent(GalleryActivity.this, UpdateProfileActivity.class);
                        intent.putExtra(GalleryActivity.PROFILE_PHOTO_PATH, photos.get(+position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    } else if (profileCoverIntent != null){
                        Intent intent = new Intent(GalleryActivity.this, UpdateProfileActivity.class);
                        intent.putExtra(GalleryActivity.PROFILE_COVER_PATH, photos.get(+position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    }else {
                        Intent intent = new Intent(GalleryActivity.this, PreviewActivity.class);
                        intent.putExtra(GalleryActivity.GALLERY_PATH, photos.get(+position).get(Function.KEY_PATH));
                        startActivity(intent);
                        finish();
                    }
                }
            });

        }

    }

    class PhotosAdapter extends BaseAdapter {
        private Activity activity;
        ArrayList<HashMap<String, String>> photos = new ArrayList<HashMap<String, String>>();


        public PhotosAdapter(Activity activity, ArrayList<HashMap<String, String>> photos) {
            this.activity = activity;
            this.photos = photos;
        }

        @Override
        public int getCount() {
            return photos.size();
        }

        @Override
        public Object getItem(int i) {
            return i;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            PicsViewHolder picsViewHolder = null;
            if (view == null){
                picsViewHolder = new PicsViewHolder();
                view = LayoutInflater.from(activity).inflate(R.layout.picture_layout, viewGroup, false);

                picsViewHolder.picsImageView = (ImageView) view.findViewById(R.id.picImageView);
                view.setTag(picsViewHolder);
            }else {
                picsViewHolder = (PicsViewHolder) view.getTag();
            }

            picsViewHolder.picsImageView.setId(i);

            HashMap<String, String> pics = new HashMap<>();
            pics = photos.get(i);

            try {
                Glide.with(GalleryActivity.this)
                        .load(new File(pics.get(Function.KEY_PATH)))
                        .into(picsViewHolder.picsImageView);
            }catch (Exception e){}

            return view;
        }

    }

    class PicsViewHolder{
        ImageView picsImageView;
    }
}
