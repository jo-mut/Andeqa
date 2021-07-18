package com.andeka.andeka.creation;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.main.HomeActivity;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.models.QueryOptions;
import com.andeka.andeka.profile.ProfileActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DialogProgressFragment extends DialogFragment{
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.progressTextView)TextView mProgressTextView;

    private static final String EXTRA_USER_UID = "uid";
    private static final String HEIGHT = "height";
    private static final String TITLE = "title";
    private static final String DESCRIPTION = "description";
    private static final String WIDTH = "width";
    private static final String IMAGE_PATH ="image path";
    private static final String VIDEO_PATH = "video path";
    private static final String COLLECTION_ID = "collection id";
    private static final String POST_ID = "post id";
    private String mCollectionId;
    private String collectionId;
    private String mPostId;
    private String height;
    private String width;
    private String title;
    private String image;
    private String video;
    private String description;
    private int progress = 0;
    private Handler handler = new Handler();
    private FirebaseAuth firebaseAuth;
    //FIRESTORE
    private CollectionReference postsCollection;
    private DatabaseReference randomReference;
    private StorageReference storageReference;
    private CollectionReference queryOptionsReference;


    public DialogProgressFragment() {
        // Required empty public constructor
    }


    public static DialogProgressFragment newInstance(String title) {
        DialogProgressFragment fragment = new DialogProgressFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dialog_progress, container, false);
        ButterKnife.bind(this, view);

        Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.progress_drawable);

        mProgressBar.setProgress(0);
        mProgressBar.setSecondaryProgress(100);
        mProgressBar.setMax(100);
        mProgressBar.setProgressDrawable(drawable);

        //init firebase references
        initFirebaseReferences();
        // get intent extras
        getIntentExtras();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCanceledOnTouchOutside(false);
        }
    }


    private void getIntentExtras() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            title = bundle.getString(DialogProgressFragment.TITLE);
            description = bundle.getString(DialogProgressFragment.DESCRIPTION);
            width = bundle.getString(DialogProgressFragment.WIDTH);
            height = bundle.getString(DialogProgressFragment.HEIGHT);
            image = bundle.getString(DialogProgressFragment.IMAGE_PATH);
            video= bundle.getString(DialogProgressFragment.VIDEO_PATH);
            mCollectionId= bundle.getString(DialogProgressFragment.COLLECTION_ID);
            mPostId= bundle.getString(DialogProgressFragment.POST_ID);


            if (image != null){
                imagePost();
            }else {
                videoPost();
            }

        }

    }

    private void initFirebaseReferences() {
        //initialize firestore
        firebaseAuth =  FirebaseAuth.getInstance();
        //get the reference to posts(collection reference)
        postsCollection = FirebaseFirestore.getInstance().collection(Constants.POSTS);
        randomReference = FirebaseDatabase.getInstance().getReference(Constants.RANDOM_PUSH_ID);
        queryOptionsReference = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
    }


    private void imagePost(){
        mProgressBar.setVisibility(View.VISIBLE);
        mProgressTextView.setText("Uploading your post");
        //current time
        final long timeStamp = new Date().getTime();
        //this push id is to put all the singles together under the same reference
        final DatabaseReference collectiveRef = randomReference.push();
        final String pushId = collectiveRef.getKey();

        // create the collection id
        if (mCollectionId != null){
            collectionId = mCollectionId;
        }else if (mPostId != null){
            collectionId = mPostId;
        }else {
            collectionId = pushId;
        }
        //this push id is to add new single to the same reference
        final DatabaseReference singleRef = randomReference.push();
        final String post_id = singleRef.getKey();

        Uri uri = Uri.fromFile(new File(image));

        if (uri != null){
            storageReference = FirebaseStorage
                    .getInstance().getReference()
                    .child(Constants.COLLECTIONS)
                    .child(Constants.IMAGES)
                    .child(post_id);

            UploadTask uploadTask = storageReference.putFile(uri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();

                        Post post = new Post();
                        postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {
                                Log.d("posts count", snapshots.size() + "");
                                final int size = snapshots.size();
                                final int number = size + 1;
                                final double random = new Random().nextDouble();

                                final Post post = new Post();
                                post.setCollection_id(collectionId);
                                post.setType("image");
                                post.setPost_id(post_id);
                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                post.setRandom_number(random);
                                post.setNumber(number);
                                post.setDeeplink("");
                                post.setTime(timeStamp);
                                post.setWidth(width);
                                post.setHeight(height);
                                post.setTitle(title);
                                post.setDescription(description);
                                post.setUrl(downloadUri.toString());


                                final String titleToLowercase [] = title.toLowerCase().split(" ");
                                final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                                QueryOptions queryOptions = new QueryOptions();
                                queryOptions.setOption_id(post_id);
                                queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                queryOptions.setType("post");
                                queryOptions.setOne(Arrays.asList(titleToLowercase));
                                queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                                queryOptionsReference.document(post_id)
                                        .set(queryOptions).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postsCollection.document(post_id).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressTextView.setText("Finishing up");
                                                //launch the collections activity
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                        intent.putExtra(DialogProgressFragment.EXTRA_USER_UID, ProfileActivity.class);
                                                        intent.putExtra(DialogProgressFragment.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                                        startActivity(intent);
                                                        dismiss();
                                                    }
                                                }, 100);
                                            }
                                        });
                                    }
                                });

                            }
                        });


                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText( getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    final double progression = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progress = (int) progression;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (progress < 100){
                                progress += 1;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(progress);
                                    }
                                });

                                if (progress == 100){
                                    mProgressBar.setIndeterminate(true);
                                }


                                try {
                                    Thread.sleep(100);
                                }catch (InterruptedException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }).start();

                }
            });

        }
    }

    private void videoPost(){
        //current time
        final long timeStamp = new Date().getTime();
        //this push id is to put all the singles together under the same reference
        final DatabaseReference collectiveRef = randomReference.push();
        final String pushId = collectiveRef.getKey();
        // create the collection id
        if (mCollectionId != null){
            collectionId = mCollectionId;
        }else {
            collectionId = pushId;
        }
        //this push id is to add new single to the same reference
        final DatabaseReference singleRef = randomReference.push();
        final String post_id = singleRef.getKey();

        storageReference = FirebaseStorage
                .getInstance().getReference()
                .child(Constants.COLLECTIONS)
                .child(Constants.VIDEOS)
                .child(post_id);

        Uri uri = Uri.fromFile(new File(video));

        if (uri != null){
            UploadTask uploadTask = storageReference.putFile(uri);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        final Uri downloadUri = task.getResult();

                        Post post = new Post();
                        postsCollection.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                            @Override
                            public void onSuccess(QuerySnapshot snapshots) {
                                final int size = snapshots.size();
                                final int number = size + 1;
                                final double random = new Random().nextDouble();

                                final Post post = new Post();
                                post.setCollection_id(collectionId);
                                post.setType("video");
                                post.setPost_id(post_id);
                                post.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                post.setRandom_number(random);
                                post.setNumber(number);
                                post.setDeeplink("");
                                post.setTime(timeStamp);
                                post.setWidth(width);
                                post.setHeight(height);
                                post.setTitle(title);
                                post.setDescription(description);
                                post.setUrl(downloadUri.toString());


                                final String titleToLowercase [] = title.toLowerCase().split(" ");
                                final String descriptionToLowercase [] = description.toLowerCase().split(" ");

                                QueryOptions queryOptions = new QueryOptions();
                                queryOptions.setOption_id(post_id);
                                queryOptions.setUser_id(firebaseAuth.getCurrentUser().getUid());
                                queryOptions.setType("post");
                                queryOptions.setOne(Arrays.asList(titleToLowercase));
                                queryOptions.setTwo(Arrays.asList(descriptionToLowercase));
                                queryOptionsReference.document(post_id).set(queryOptions);
                                postsCollection.document(post_id).set(post);

                                queryOptionsReference.document(post_id)
                                        .set(queryOptions).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        postsCollection.document(post_id).set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProgressTextView.setText("Finishing up");
                                                //launch the collections activity
                                                new Handler().postDelayed(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        Intent intent = new Intent(getActivity(), HomeActivity.class);
                                                        intent.putExtra(DialogProgressFragment.EXTRA_USER_UID, ProfileActivity.class);
                                                        intent.putExtra(DialogProgressFragment.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
                                                        startActivity(intent);
                                                        dismiss();
                                                    }
                                                }, 100);
                                            }
                                        });
                                    }
                                });
                            }
                        });


                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    final double progression = (100 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                    progress = (int) progression;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (progress < 100){
                                progress += 1;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setProgress(progress);
                                    }
                                });

                                if (progress == 100){
                                    mProgressBar.setIndeterminate(true);
                                }


                                try {
                                    Thread.sleep(100);
                                }catch (InterruptedException ex){
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }).start();

                }
            });

        }

    }
}
