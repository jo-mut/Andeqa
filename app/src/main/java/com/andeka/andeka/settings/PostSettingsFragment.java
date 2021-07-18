package com.andeka.andeka.settings;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.models.Post;
import com.andeka.andeka.post_detail.PostDetailActivity;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.app.Activity.RESULT_OK;


public class PostSettingsFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    @Bind(R.id.shareRelativeLayout)RelativeLayout mShareViaLinearLayout;
    @Bind(R.id.deleteRelativeLayout)RelativeLayout mDeleteLinearLayout;
    @Bind(R.id.editRelativeLayout)RelativeLayout mRedeemLinearLayout;
    @Bind(R.id.reportRelativeLayout)RelativeLayout mReportLinearLayout;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference postsCollections;
    private static final String TAG = PostSettingsFragment.class.getSimpleName();

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TYPE = "type";
    private static final String EXTRA_URI = "uri";
    private static final String DELETE_POST = "delete post";
    private static final int REQUEST_INVITE = 0;


    private String mType;
    private String mCollectionId;
    private String mUid;
    private String mPostId;
    private String mUri;


    private PostDetailActivity mPostDetailActivity;

    public static PostSettingsFragment newInstance() {
        final PostSettingsFragment fragment = new PostSettingsFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(PostSettingsFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);
        mPostDetailActivity = (PostDetailActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_settings, container, false);
        ButterKnife.bind(this,  view);

        mDeleteLinearLayout.setOnClickListener(this);
        mReportLinearLayout.setOnClickListener(this);
        mRedeemLinearLayout.setOnClickListener(this);
        mShareViaLinearLayout.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        Bundle bundle = getArguments();
        if (bundle != null){
            mCollectionId = bundle.getString(PostSettingsFragment.COLLECTION_ID);
            mPostId = bundle.getString(PostSettingsFragment.EXTRA_POST_ID);
            mType = bundle.getString(PostSettingsFragment.TYPE);

            mShareViaLinearLayout.setVisibility(View.VISIBLE);

            postsCollections = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        final Post post = documentSnapshot.toObject(Post.class);
                        if (post.getDeeplink() != null){
                            if (post.getUser_id().equals(firebaseAuth.getCurrentUser().getUid())){
//                                mRedeemLinearLayout.setVisibility(View.VISIBLE);
                                mDeleteLinearLayout.setVisibility(View.VISIBLE);
                            }else {
//                                mReportLinearLayout.setVisibility(View.VISIBLE);
                            }
                        }else {
                            if (post.getUser_id().equals(firebaseAuth.getCurrentUser().getUid())){
//                                mRedeemLinearLayout.setVisibility(View.VISIBLE);
                                mDeleteLinearLayout.setVisibility(View.VISIBLE);
                            }else {
//                                mReportLinearLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }
            });



        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.setCanceledOnTouchOutside(true);
        }

    }

    @Override
    public void onClick(View v){

        if (v == mDeleteLinearLayout){
            deleteCingle();
        }

        if (v == mShareViaLinearLayout){
            mProgressBar.setVisibility(View.VISIBLE);
            mUri = mPostDetailActivity.shareUri();
            postsCollections.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot,
                                    @javax.annotation.Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        final Post post = documentSnapshot.toObject(Post.class);
                        if (post.getDeeplink() != null){
                            final String text = "View this photo with Andeqa ";
                            if (mUri != null){
                                String linkedText = text + " " + post.getDeeplink();
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_TEXT, linkedText);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mUri));
                                shareIntent.setType("image/*");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent, "Share with your "));
                            }
                        }else {
                            if (mUri != null){
                                Intent shareIntent = new Intent();
                                shareIntent.setAction(Intent.ACTION_SEND);
                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(mUri));
                                shareIntent.setType("image/*");
                                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                startActivity(Intent.createChooser(shareIntent, "Share with your "));
                            }
                        }
                    }
                }
            });
        }

    }

    public void deleteCingle(){
        // delete post in collection and delete post in overall document
        Bundle bundle = new Bundle();
        bundle.putString(PostSettingsFragment.EXTRA_POST_ID, mPostId);
        bundle.putString(PostSettingsFragment.COLLECTION_ID, mCollectionId);
        bundle.putString(PostSettingsFragment.TYPE, mType);
        bundle.putString(PostSettingsFragment.DELETE_POST, "delete post");
        FragmentManager fragmenManager = getChildFragmentManager();
        ConfirmDeleteFragment confirmDeleteFragment =  ConfirmDeleteFragment.newInstance("post settings");
        confirmDeleteFragment.setArguments(bundle);
        confirmDeleteFragment.show(fragmenManager, "market post settings fragment");

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_INVITE && data != null) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            }else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }


    }

//    private void onInviteClicked(String message, String deeplink, String image) {
//        Intent intent = new AppInviteInvitation.IntentBuilder("Invites")
//                .setMessage(message)
//                .setDeepLink(Uri.parse(deeplink))
//                .setCustomImage(Uri.parse(image))
//                .setCallToActionText("")
//                .build();
//        startActivityForResult(intent, REQUEST_INVITE);
//    }

}
