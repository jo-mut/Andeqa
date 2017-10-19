package com.cinggl.cinggl.home;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.cinggl.cinggl.ifair.RedeemCreditsDialogFragment;
import com.cinggl.cinggl.ifair.ListOnIfairActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleSettingsDialog extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.deleteCingleRelativeLayout)RelativeLayout mDeleteCingleRelativeLayout;
//    @Bind(R.id.editCingleRelativeLayout)RelativeLayout mEditCingleRelativeLayout;
    @Bind(R.id.tradeCingleRelativeLayout)RelativeLayout mTradeCingleRelativeLayout;
//    @Bind(R.id.reportCingleRelativeLayout)RelativeLayout mReportCingleRelativeLayoout;
    @Bind(R.id.redeemCreditsRelativeLayout)RelativeLayout mRedeemCreditsRelativeLayout;
    private static final String EXTRA_POST_KEY = "post key";
    private String mPostKey;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private DatabaseReference ifairReference;
    private DatabaseReference cinglesReference;
    private DatabaseReference cingleOwnerReference;
    private DatabaseReference profileCinglesReference;
    private StorageReference storageReference;
    private Query mKeyQuery;
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
        View view = inflater.inflate(R.layout.fragment_cingle_settings_dialog, container, false);
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

            ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
            cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.POSTS);
            mKeyQuery = databaseReference;
            cingleOwnerReference = FirebaseDatabase.getInstance().getReference(Constants.CINGLE_ONWERS);
            profileCinglesReference = FirebaseDatabase.getInstance().getReference(Constants.PROFILE_CINGLES)
                    .child(firebaseAuth.getCurrentUser().getUid());
            storageReference = FirebaseStorage.getInstance().getReference(Constants.POSTS);

            ifairReference.keepSynced(true);
            cingleOwnerReference.keepSynced(true);
            profileCinglesReference.keepSynced(true);

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
            ifairReference.child("Cingle Selling").addValueEventListener
                    (new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(mPostKey)){
                       try {
                           Toast.makeText(getContext(), "Sorry! This Cingle is already on sale",
                                   Toast.LENGTH_SHORT).show();
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                    }else {
                        try {
                            Intent intent = new Intent(getActivity(), ListOnIfairActivity.class);
                            intent.putExtra(CingleSettingsDialog.EXTRA_POST_KEY, mPostKey);
                            startActivity(intent);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

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
        profileCinglesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mPostKey)){
                    profileCinglesReference.child(mPostKey).removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                cinglesReference.child(mPostKey).removeValue()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            ifairReference.child("Cingle Selling").addListenerForSingleValueEvent
                                                    (new ValueEventListener() {
                                                @Override
                                                public void onDataChange(DataSnapshot dataSnapshot) {
                                                    if (dataSnapshot.hasChild(mPostKey)){
                                                        ifairReference.child("Cingle Selling").child(mPostKey).removeValue();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(DatabaseError databaseError) {

                                                }
                                            });
                                        }

                                        storageReference.child(firebaseAuth.getCurrentUser().getUid())
                                                .child(mPostKey).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    //data has bee completely deleted
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        dismiss();
    }
}
