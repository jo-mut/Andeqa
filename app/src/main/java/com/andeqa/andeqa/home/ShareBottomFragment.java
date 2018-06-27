package com.andeqa.andeqa.home;

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
import android.widget.LinearLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.market.RedeemCreditsActivity;
import com.andeqa.andeqa.models.Post;
import com.andeqa.andeqa.settings.ConfirmDeleteFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;


public class ShareBottomFragment extends BottomSheetDialogFragment implements View.OnClickListener {
//    @Bind(R.id.directMessageLinearLayout)LinearLayout mDirectMessageLinearLayout;
//    @Bind(R.id.shareAndeqaLinearLayout)LinearLayout mShareAndeqaLinearLayout;
    @Bind(R.id.shareViaLinearLayout)LinearLayout mShareViaLinearLayout;
    @Bind(R.id.deleteLinearLayout)LinearLayout mDeleteLinearLayout;
    @Bind(R.id.redeemLinearLayout)LinearLayout mRedeemLinearLayout;

    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference marketCollections;
    private CollectionReference postsCollections;
    private CollectionReference shareCollections;
    private static final String TAG = ShareBottomFragment.class.getSimpleName();

    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TYPE = "type";
    private static final String EXTRA_USER_UID = "uid";
    private static final String EXTRA_URI = "uri";

    private String mType;
    private String mCollectionId;
    private String mUid;
    private String mPostId;
    private String mUri;


    public static ShareBottomFragment newInstance() {
        final ShareBottomFragment fragment = new ShareBottomFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_bottom, container, false);
        ButterKnife.bind(this,  view);

//        mShareAndeqaLinearLayout.setOnClickListener(this);
        mDeleteLinearLayout.setOnClickListener(this);
//        mDirectMessageLinearLayout.setOnClickListener(this);
        mRedeemLinearLayout.setOnClickListener(this);
        mShareViaLinearLayout.setOnClickListener(this);


        Bundle bundle = getArguments();
        if (bundle != null){
            mCollectionId = bundle.getString(ShareBottomFragment.COLLECTION_ID);
            mPostId = bundle.getString(ShareBottomFragment.EXTRA_POST_ID);
            mType = bundle.getString(ShareBottomFragment.TYPE);
            mUri = bundle.getString(ShareBottomFragment.EXTRA_URI);


            postsCollections = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            shareCollections = FirebaseFirestore.getInstance().collection(Constants.SHARES);


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
                            mShareViaLinearLayout.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });



        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onClick(View v){
//        if (v == mDirectMessageLinearLayout){
//
//        }

//        if (v == mShareAndeqaLinearLayout){
//
//        }

        if (v == mDeleteLinearLayout){
            deleteCingle();
        }

        if (v == mShareViaLinearLayout){
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
                        }
                    }
                }
            });
        }

        if (v == mRedeemLinearLayout){
            Intent intent = new Intent(getActivity(), RedeemCreditsActivity.class);
            intent.putExtra(ShareBottomFragment.EXTRA_POST_ID, mPostId);
            intent.putExtra(ShareBottomFragment.COLLECTION_ID, mCollectionId);
            intent.putExtra(ShareBottomFragment.TYPE, mType);
            startActivity(intent);
        }
    }

    public void deleteCingle(){
        // delete post in collection and delete post in overall document
        Bundle bundle = new Bundle();
        bundle.putString(ShareBottomFragment.EXTRA_POST_ID, mPostId);
        bundle.putString(ShareBottomFragment.COLLECTION_ID, mCollectionId);
        bundle.putString(ShareBottomFragment.TYPE, mType);
        FragmentManager fragmenManager = getChildFragmentManager();
        ConfirmDeleteFragment confirmDeleteFragment =  ConfirmDeleteFragment.newInstance("post settings");
        confirmDeleteFragment.setArguments(bundle);
        confirmDeleteFragment.show(fragmenManager, "market post settings fragment");

    }

}
