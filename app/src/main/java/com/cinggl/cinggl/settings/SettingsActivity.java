package com.cinggl.cinggl.settings;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cinggulan;
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
    @Bind(R.id.sendFeedbackRelativeLayout)RelativeLayout mSendFeedbackRelativeLayout;
    @Bind(R.id.faqRelativeLayout)RelativeLayout mFaqRelativeLayout;
    @Bind(R.id.privacyPolicyRelativeLayout)RelativeLayout mPrivacyPolicyRelativeLayout;
    @Bind(R.id.deleteAccountRelativeLayout)RelativeLayout mDeleteAccountRelativeLayout;

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

        mDeleteAccountRelativeLayout.setOnClickListener(this);
        mFaqRelativeLayout.setOnClickListener(this);
        mPrivacyPolicyRelativeLayout.setOnClickListener(this);
        mSendFeedbackRelativeLayout.setOnClickListener(this);

        setSupportActionBar(mToolBar);
        mToolBar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
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
                    final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                    String username = cinggulan.getUsername();
                    final String profileImage = cinggulan.getProfileImage();
                    String bio = cinggulan.getBio();

                    mUsernameTextView.setText(username);
                    mBioTextView.setText(bio);
                    Picasso.with(SettingsActivity.this)
                            .load(profileImage)
                            .resize(MAX_WIDTH, MAX_HEIGHT)
                            .onlyScaleDown()
                            .centerCrop()
                            .placeholder(R.drawable.profle_image_background)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mProfileImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(SettingsActivity.this)
                                            .load(profileImage)
                                            .resize(MAX_WIDTH, MAX_HEIGHT)
                                            .onlyScaleDown()
                                            .centerCrop()
                                            .placeholder(R.drawable.profle_image_background)
                                            .into(mProfileImageView);

                                }
                            });


                }

            }
        });
    }

    @Override
    public void onClick(View v){
        if (v == mDeleteAccountRelativeLayout){
            FragmentManager fragmentManager = getSupportFragmentManager();
            DialogDeleteAccountFragment dialogDeleteAccountFragment = DialogDeleteAccountFragment.newInstance("delete account");
            dialogDeleteAccountFragment.show(fragmentManager, "delete account dialog fragment");
        }

        if (v == mFaqRelativeLayout){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://johnmutuku628.wixsite.com/cinggl"));
            startActivity(intent);
        }

        if (v == mPrivacyPolicyRelativeLayout){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://johnmutuku628.wixsite.com/cinggl"));
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
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cinggl@yahoo.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
            intent.putExtra(Intent.EXTRA_TEXT, body);
            this.startActivity(Intent.createChooser(intent, this.getString(R.string.choose_email_client)));
        }
    }

}