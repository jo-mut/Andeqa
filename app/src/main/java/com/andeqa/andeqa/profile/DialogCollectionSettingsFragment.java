package com.andeqa.andeqa.profile;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class DialogCollectionSettingsFragment extends DialogFragment {


    public DialogCollectionSettingsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dialog_collection_settings, container, false);
    }

}
