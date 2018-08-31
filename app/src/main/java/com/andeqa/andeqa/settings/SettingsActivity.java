package com.andeqa.andeqa.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.Andeqan;
import com.andeqa.andeqa.profile.DialogConfirmSingOutFragment;
import com.andeqa.andeqa.profile.ProfileActivity;
import com.andeqa.andeqa.profile.UpdateProfileActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.toolbar)Toolbar mToolBar;
    @Bind(R.id.profileImageView)CircleImageView mProfileImageView;
    @Bind(R.id.usernameTextView)TextView mUsernameTextView;
    @Bind(R.id.bioTextView)TextView mBioTextView;
    @Bind(R.id.sendFeedbackRelativeLayout)LinearLayout mSendFeedbackRelativeLayout;
//    @Bind(R.id.faqRelativeLayout)RelativeLayout mFaqRelativeLayout;
    @Bind(R.id.privacyPolicyRelativeLayout)LinearLayout mPrivacyPolicyRelativeLayout;
    @Bind(R.id.updateProfileRelativeLayout)LinearLayout mUpdateProfileRelativeLayout;
//    @Bind(R.id.deleteAccountRelativeLayout)RelativeLayout mDeleteAccountRelativeLayout;
    @Bind(R.id.signOutLinearLayout)LinearLayout mSignOutLinearLayout;

    private static final String TAG = SettingsActivity.class.getSimpleName();
    private CollectionReference usersCollection;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private  static final int MAX_WIDTH = 200;
    private static final int MAX_HEIGHT = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

//        mDeleteAccountRelativeLayout.setOnClickListener(this);
//        mFaqRelativeLayout.setOnClickListener(this);
        mPrivacyPolicyRelativeLayout.setOnClickListener(this);
        mSendFeedbackRelativeLayout.setOnClickListener(this);
        mUpdateProfileRelativeLayout.setOnClickListener(this);
        mSignOutLinearLayout.setOnClickListener(this);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth != null){
            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        }

        getProfile();
    }


    private void getProfile(){
        usersCollection.document(firebaseAuth.getCurrentUser().getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Andeqan andeqan = documentSnapshot.toObject(Andeqan.class);
                    String username = andeqan.getUsername();
                    final String profileImage = andeqan.getProfile_image();
                    String bio = andeqan.getBio();

                    mUsernameTextView.setText(username);
                    mBioTextView.setText(bio);

                    Glide.with(getApplicationContext())
                            .load(profileImage)
                            .apply(new RequestOptions()
                                    .placeholder(R.drawable.ic_user_white)
                                    .diskCacheStrategy(DiskCacheStrategy.DATA))
                            .into(mProfileImageView);


                }

            }
        });
    }

    @Override
    public void onClick(View v){
//        if (v == mDeleteAccountRelativeLayout){
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            DeleteAccountFragment dialogDeleteAccountFragment = DeleteAccountFragment.newInstance("delete account");
//            dialogDeleteAccountFragment.show(fragmentManager, "delete account dialog fragment");
//        }

//        if (v == mFaqRelativeLayout){
//            Intent intent = new Intent(Intent.ACTION_VIEW,
//                    Uri.parse("https://andeqa@andeqa.com"));
//            startActivity(intent);
//        }

        if (v == mPrivacyPolicyRelativeLayout){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://andeqa.com"));
            startActivity(intent);
        }

        if ( v == mUpdateProfileRelativeLayout){
            Intent intent = new Intent(SettingsActivity.this, UpdateProfileActivity.class);
            startActivity(intent);
        }

        if (v == mSendFeedbackRelativeLayout){
            String body = null;
            try {
                body = this.getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName;
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


        if (v == mSignOutLinearLayout){
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogConfirmSingOutFragment dialogConfirmSingOutFragment = DialogConfirmSingOutFragment.newInstance("sing out");
            dialogConfirmSingOutFragment.show(fragmentManager, "delete account fragment");
        }
    }

}
