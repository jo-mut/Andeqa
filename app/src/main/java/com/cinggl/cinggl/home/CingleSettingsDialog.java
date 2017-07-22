package com.cinggl.cinggl.home;


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
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleSettingsDialog extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.deleteCingleRelativeLayout)RelativeLayout mDeleteCingleRelativeLayout;
    private static final String EXTRA_POST_KEY = "post key";
    private String mKey;
    private DatabaseReference databaseReference;
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

        mDeleteCingleRelativeLayout.setOnClickListener(this);


        Bundle bundle = getArguments();
        if (bundle != null){
            mKey = bundle.getString(CingleSettingsDialog.EXTRA_POST_KEY);

            Log.d("the passed poskey", mKey);

        }else {
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        databaseReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "cingle settings");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v){
        if (v == mDeleteCingleRelativeLayout){
            deleteCingle();
        }
    }

    public void deleteCingle(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(mKey)){
                    databaseReference.child(mKey).removeValue();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
