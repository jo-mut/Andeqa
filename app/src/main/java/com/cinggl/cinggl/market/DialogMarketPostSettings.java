package com.cinggl.cinggl.market;


import android.os.Bundle;
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

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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

    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String TAG = DialogMarketPostSettings.class.getSimpleName();

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
        }

        Bundle bundle = getArguments();
        if (bundle != null){
            mPostKey = bundle.getString(DialogMarketPostSettings.EXTRA_POST_KEY);

            Log.d("the passed poskey", mPostKey);

        }else {
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }



        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "post settings credits");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onClick(View v){
        if (v == mYesRelativeLayout){
            sellingCollection.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                    if (e != null) {
                        Log.w(TAG, "Listen error", e);
                        return;
                    }

                    if (documentSnapshot.exists()){
                        sellingCollection.document(mPostKey).delete();
                    }
                }
            });
        }

        if (v == mNoRelativeLayout){
            dismiss();
        }
    }

}
