package com.cinggl.cinggl.preferences;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.market.RedeemCreditsDialogFragment;
import com.cinggl.cinggl.market.ListOnMarketActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleSettingsDialog extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.deletePostRelativeLayout)RelativeLayout mDeleteCingleRelativeLayout;
//    @Bind(R.id.editCingleRelativeLayout)RelativeLayout mEditCingleRelativeLayout;
    @Bind(R.id.tradePostRelativeLayout)RelativeLayout mTradeCingleRelativeLayout;
//    @Bind(R.id.reportCingleRelativeLayout)RelativeLayout mReportCingleRelativeLayoout;
    @Bind(R.id.redeemCreditsRelativeLayout)RelativeLayout mRedeemCreditsRelativeLayout;
    private static final String EXTRA_POST_KEY = "post key";
    private String mPostKey;
    //firebase auth
    private FirebaseAuth firebaseAuth;
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference senseCreditReference;
    private CollectionReference ifairReference;
    //firebase
    private DatabaseReference cingleOwnerReference;
    //firebase storage
    private StorageReference storageReference;
    private static final String TAG = CingleSettingsDialog.class.getSimpleName();

    public static CingleSettingsDialog newInstance(String title){
        CingleSettingsDialog cingleSettingsDialog = new CingleSettingsDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        cingleSettingsDialog.setArguments(args);
        return cingleSettingsDialog;
    }


    public CingleSettingsDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_post_settings_dialog, container, false);
        ButterKnife.bind(this, view);

        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mDeleteCingleRelativeLayout.setOnClickListener(this);
//            mReportCingleRelativeLayoout.setOnClickListener(this);
//            mEditCingleRelativeLayout.setOnClickListener(this);
            mTradeCingleRelativeLayout.setOnClickListener(this);
            mRedeemCreditsRelativeLayout.setOnClickListener(this);

            Bundle bundle = getArguments();
            if (bundle != null){
                mPostKey = bundle.getString(CingleSettingsDialog.EXTRA_POST_KEY);

                Log.d("the passed poskey", mPostKey);

            }else {
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            //firebase
            cingleOwnerReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);
            //firestore
            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);
            // firebase storage
            storageReference = FirebaseStorage.getInstance().getReference(Constants.POSTS);

            cingleOwnerReference.keepSynced(true);


        }

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "cingle settings");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View v){
        if (v == mDeleteCingleRelativeLayout){
            deleteCingle();
        }

//        if (v == mReportCingleRelativeLayoout){
//
//        }

//        if (v == mEditCingleRelativeLayout){
//            editCingle();
//        }

        if (v == mTradeCingleRelativeLayout){
            ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        try {
                            Toast.makeText(getContext(), "This Post is already on sale",
                                    Toast.LENGTH_SHORT).show();
                        }catch (Exception ex){
                            ex.printStackTrace();
                        }
                    }else {
                        Intent intent = new Intent(getActivity(), ListOnMarketActivity.class);
                        intent.putExtra(CingleSettingsDialog.EXTRA_POST_KEY, mPostKey);
                        startActivity(intent);
                    }
                }
            });

        }

        if (v == mRedeemCreditsRelativeLayout){
            //LAUCH THE DIALOG TO REDEEM CREDITS
            Bundle bundle = new Bundle();
            bundle.putString(CingleSettingsDialog.EXTRA_POST_KEY, mPostKey);
            FragmentManager fragmenManager = getChildFragmentManager();
            RedeemCreditsDialogFragment redeemCreditsDialogFragment = RedeemCreditsDialogFragment
                    .newInstance("redeem credits");
            redeemCreditsDialogFragment.setArguments(bundle);
            redeemCreditsDialogFragment.show(fragmenManager, "redeem cingle cscs");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        dismiss();
    }

    public void deleteCingle(){
        cinglesReference.document(mPostKey)
                .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                senseCreditReference.document(mPostKey).delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                ifairReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                            }
                        });

            }
        });

        dismiss();
    }
}
