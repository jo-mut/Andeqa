package com.andeqa.andeqa.settings;


import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeSettingsFragment extends BottomSheetDialogFragment
        implements View.OnClickListener {
    @Bind(R.id.profileImageView)CircleImageView profileImageView;
    @Bind(R.id.profileCoverImageView)ImageView profileCoverImageView;
    @Bind(R.id.fullNameTextView)TextView  fullnameTextView;
    @Bind(R.id.emailTextView)TextView emailTextView;
    @Bind(R.id.settingsImageView) ImageView settingsImageView;
    @Bind(R.id.aboutImageView)ImageView aboutImageView;
    @Bind(R.id.feedbackImageView)ImageView feedbackImageView;
    @Bind(R.id.shareImageView)ImageView shareImageView;

    private static final String EXTRA_USER_UID =  "uid";
    private FirebaseAuth firebaseAuth;
    private CollectionReference usersReference;
    private static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;
    private static final int IMAGE_GALLERY_REQUEST = 112;

    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private static final String TAG = HomeSettingsFragment.class.getSimpleName();

    public static HomeSettingsFragment newInstance() {
        HomeSettingsFragment fragment = new HomeSettingsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    public HomeSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(HomeSettingsFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_settings, container, false);
        usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        ButterKnife.bind(this, view);
        profileImageView.setOnClickListener(this);
        settingsImageView.setOnClickListener(this);
        aboutImageView.setOnClickListener(this);
        feedbackImageView.setOnClickListener(this);
        shareImageView.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fetchData();

        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == profileImageView){
            Intent intent = new Intent(getActivity(), ProfileActivity.class);
            intent.putExtra(HomeSettingsFragment.EXTRA_USER_UID, firebaseAuth.getCurrentUser().getUid());
            startActivity(intent);
        }

        if (v == settingsImageView){
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        }

        if (v == feedbackImageView){
            String body = null;
            try {
                body = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0).versionName;
                body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                        Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                        "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
            } catch (PackageManager.NameNotFoundException e) {
            }
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"andeqa@andeqa.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
            intent.putExtra(Intent.EXTRA_TEXT, body);
            this.startActivity(Intent.createChooser(intent, this.getString(R.string.choose_email_client)));
        }

        if (v== shareImageView){
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT,
                    "Come join me at Andeqa, a beautiful photo sharing app" +
                            " at: https://play.google.com/store/apps/details?id=com.andeqa.andeqa");
            sendIntent.setType("text/plain");
            startActivity(sendIntent);
        }

        if (v == aboutImageView){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://andeqa.com"));
            startActivity(intent);
        }
    }

    private void fetchData(){
        //database references
        usersReference.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                            String firstName = andeqan.getFirst_name();
                            String secondName = andeqan.getSecond_name();
                            final String profileImage = andeqan.getProfile_image();
                            final String profileCover = andeqan.getProfile_cover();
                            final String email = andeqan.getEmail();

                            fullnameTextView.setText(firstName + " " + secondName);
                            emailTextView.setText(email);

                            Glide.with(getContext())
                                    .load(profileImage)
                                    .apply(new RequestOptions()
                                            .placeholder(R.drawable.ic_user_white)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(profileImageView);

                            Glide.with(getContext())
                                    .load(profileCover)
                                    .apply(new RequestOptions()
                                            .diskCacheStrategy(DiskCacheStrategy.DATA))
                                    .into(profileCoverImageView);

                        }
                    }
                });

    }
}
