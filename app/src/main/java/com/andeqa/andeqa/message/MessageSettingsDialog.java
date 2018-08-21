package com.andeqa.andeqa.message;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andeqa.andeqa.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MessageSettingsDialog extends Fragment {


    public MessageSettingsDialog() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_message_settings_dialog, container, false);

        return view;
    }

}
