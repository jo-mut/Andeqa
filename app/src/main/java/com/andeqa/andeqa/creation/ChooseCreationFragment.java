package com.andeqa.andeqa.creation;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.camera.CameraActivity;
import com.andeqa.andeqa.camera.PicturesActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChooseCreationFragment extends BottomSheetDialogFragment implements View.OnClickListener {
    @Bind(R.id.galleryRelativeLayout)RelativeLayout mGalleryRelativeLayout;
    @Bind(R.id.collectionRelativeLayout)RelativeLayout mCollectionRelativeLayout;
    @Bind(R.id.cameraRelativeLayout)RelativeLayout mCameraRelativeLayout;


    public static ChooseCreationFragment newInstance() {
        final ChooseCreationFragment fragment = new ChooseCreationFragment();
        final Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ChooseCreationFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(ChooseCreationFragment.STYLE_NORMAL, R.style.Theme_AppCompat_Translucent);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_choose_creation, container, false);
        ButterKnife.bind(this, view);

        mCameraRelativeLayout.setOnClickListener(this);
        mGalleryRelativeLayout.setOnClickListener(this);
        mCollectionRelativeLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.setCanceledOnTouchOutside(true);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mCameraRelativeLayout){
            Intent intent = new Intent(getContext(), CameraActivity.class);
            startActivity(intent);
        }

        if (v == mGalleryRelativeLayout){
            Intent intent = new Intent(getContext(), PicturesActivity.class);
            startActivity(intent);
        }

        if (v == mCollectionRelativeLayout){
            Intent intent = new Intent(getContext(), CreateCollectionActivity.class);
            startActivity(intent);
        }

    }
}
