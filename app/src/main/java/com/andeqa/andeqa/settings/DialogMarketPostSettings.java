package com.andeqa.andeqa.settings;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.market.ListOnMarketActivity;
import com.andeqa.andeqa.models.Market;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogMarketPostSettings extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.noRelativeLayout)RelativeLayout mNoRelativeLayout;
    @Bind(R.id.YesRelativeLayout)RelativeLayout mYesRelativeLayout;

    private CollectionReference sellingCollection;
    private FirebaseAuth firebaseAuth;

    private String mPostId;
    private String mCollectionId;
    private static final String COLLECTION_ID = "collection id";
    private static final String EXTRA_POST_ID = "post id";
    private static final String TAG = DialogMarketPostSettings.class.getSimpleName();
    private ProgressDialog progressDialog;

    public static DialogMarketPostSettings newInstance(String title){
        DialogMarketPostSettings dialogMarketPostSettings = new DialogMarketPostSettings();
        Bundle args = new Bundle();
        args.putString("title", title);
        dialogMarketPostSettings.setArguments(args);
        return dialogMarketPostSettings;

    }


    public DialogMarketPostSettings() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_market_post_settings_fragement, container, false);
        ButterKnife.bind(this, view);

        mNoRelativeLayout.setOnClickListener(this);
        mYesRelativeLayout.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){
            sellingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);

            updatingSalePrice();
        }

        Bundle bundle = getArguments();
        if (bundle != null){
            mCollectionId = bundle.getString(DialogMarketPostSettings.COLLECTION_ID);
            mPostId = bundle.getString(DialogMarketPostSettings.EXTRA_POST_ID);


        }else {
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }



        return view;
    }


    public void updatingSalePrice(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting from the market...");
        progressDialog.setCancelable(true);
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            String title = getArguments().getString("title", "post market settings");
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    @Override
    public void onClick(View v){
        if (v == mYesRelativeLayout){
            progressDialog.show();
            sellingCollection.document(mPostId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        sellingCollection.document(mPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                try {
                                    Toast.makeText(getContext(), "Your post has been deleted from the market",
                                            Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dismiss();
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        });

                    }
                }
            });

        }

        if (v == mNoRelativeLayout){
            dismiss();
        }
    }

}
