package com.cinggl.cinggl.home;


;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class CingleSettingsDialogFragment extends DialogFragment implements View.OnClickListener {
    @Bind(R.id.deleteThisCingleLayout)RelativeLayout mDeleteThisCingleLayout;
    @Bind(R.id.blockThisCingleLayout)RelativeLayout mBlockedThisCingleLayout;

    public CingleSettingsDialogFragment() {
        // Required empty public constructor
    }

    public static CingleSettingsDialogFragment newInstance(String title){
        CingleSettingsDialogFragment cingleSettingsDialogFragment = new CingleSettingsDialogFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        cingleSettingsDialogFragment.setArguments(args);
        return cingleSettingsDialogFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_cingle_settings_dialog, container, false);
        ButterKnife.bind(this, view);

        mBlockedThisCingleLayout.setOnClickListener(this);
        mDeleteThisCingleLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        String title = getArguments().getString("title", "create your cingle");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v){
        if (v == mBlockedThisCingleLayout){

        }

        if (v == mDeleteThisCingleLayout){

        }

    }

}
