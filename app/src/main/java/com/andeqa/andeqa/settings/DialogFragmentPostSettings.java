package com.andeqa.andeqa.settings;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.market.DialogRedeemCredits;
import com.andeqa.andeqa.market.ListOnMarketActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.google.firebase.firestore.FieldValue.delete;


/**
 * Created by J.EL on 2/6/2018.
 */

public class DialogFragmentPostSettings extends DialogFragment implements View.OnClickListener {

    @Bind(R.id.deletePostRelativeLayout)RelativeLayout mDeleteCingleRelativeLayout;
    @Bind(R.id.tradePostRelativeLayout)RelativeLayout mTradeCingleRelativeLayout;
    @Bind(R.id.redeemCreditsRelativeLayout)RelativeLayout mRedeemCreditsRelativeLayout;

    private static final String EXTRA_POST_KEY = "post key";
    private String mPostKey;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference senseCreditReference;
    private CollectionReference ifairReference;
    private CollectionReference ownerReference;
    private static final String TAG = DialogFragmentPostSettings.class.getSimpleName();


    public static DialogFragmentPostSettings newInstance(String title){
        DialogFragmentPostSettings dialogFragmentPostSettings = new DialogFragmentPostSettings();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialogFragmentPostSettings.setArguments(args);
        return dialogFragmentPostSettings;

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_fragment_post_settings, container, false);

        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mDeleteCingleRelativeLayout.setOnClickListener(this);
            mTradeCingleRelativeLayout.setOnClickListener(this);
            mRedeemCreditsRelativeLayout.setOnClickListener(this);

            Bundle bundle = getArguments();
            if (bundle != null){
                mPostKey = bundle.getString(DialogFragmentPostSettings.EXTRA_POST_KEY);

                Log.d("the passed poskey", mPostKey);

            }else {
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.SELLING);
            ownerReference = FirebaseFirestore.getInstance().collection(Constants.POST_OWNERS);

            showSaleLayout();

        }

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
    }

    @Override
    public void onClick(View v){
        if (v == mDeleteCingleRelativeLayout){
            deleteCingle();
        }

        if (v == mTradeCingleRelativeLayout){
            Intent intent = new Intent(getActivity(), ListOnMarketActivity.class);
            intent.putExtra(DialogFragmentPostSettings.EXTRA_POST_KEY, mPostKey);
            startActivity(intent);
        }

        if (v == mRedeemCreditsRelativeLayout){
            //LAUCH THE DIALOG TO REDEEM CREDITS
            Bundle bundle = new Bundle();
            bundle.putString(DialogFragmentPostSettings.EXTRA_POST_KEY, mPostKey);
            FragmentManager fragmentManager = getChildFragmentManager();
            DialogRedeemCredits dialogRedeemCredits = DialogRedeemCredits
                    .newInstance("redeem credits");
            dialogRedeemCredits.setArguments(bundle);
            dialogRedeemCredits.show(fragmentManager, "redeem post credits");
        }

    }

    public void showSaleLayout(){
        ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    mTradeCingleRelativeLayout.setVisibility(View.GONE);
                }else {
                    mTradeCingleRelativeLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }


    public void deleteCingle(){
        cinglesReference.document(mPostKey)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    cinglesReference.document(mPostKey).delete();
                }
            }
        });

        ownerReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    ownerReference.document(mPostKey).delete();
                }
            }
        });
        senseCreditReference.document(mPostKey)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            senseCreditReference.document(mPostKey).delete();
                        }
                    }
                });

        ifairReference.document(mPostKey)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()) {
                            ifairReference.document(mPostKey).delete();
                        }
                    }
                });

        dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}