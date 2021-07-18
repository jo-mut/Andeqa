package com.andeka.andeka.profile;


import android.content.Intent;
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

import com.andeka.andeka.R;
import com.andeka.andeka.registration.SignInActivity;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ConfirmSingOutFragment extends DialogFragment implements View.OnClickListener {
    @Bind(R.id.noRelativeLayout)RelativeLayout mNoRelativeLayout;
    @Bind(R.id.YesRelativeLayout)RelativeLayout mYesRelativeLayout;

    private FirebaseAuth firebaseAuth;


    public static ConfirmSingOutFragment newInstance(String title){
        ConfirmSingOutFragment confirmSingOutFragment = new ConfirmSingOutFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        confirmSingOutFragment.setArguments(args);
        return confirmSingOutFragment;

    }


    public ConfirmSingOutFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_confirm_sing_out, container, false);
        ButterKnife.bind(this, view);


        mYesRelativeLayout.setOnClickListener(this);
        mNoRelativeLayout.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String title = getArguments().getString("title", "sign out");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    @Override
    public void onClick(View v){
        if ( v == mYesRelativeLayout){
            firebaseAuth.signOut();
            Intent intent = new Intent(getActivity(), SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }

        if (v == mNoRelativeLayout){
            dismiss();
        }
    }
}
