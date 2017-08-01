package com.cinggl.cinggl.home;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cinggl.cinggl.App;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.profile.UpdateProfileActivity;
import com.cinggl.cinggl.services.ConnectivityReceiver;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.Bind;
import butterknife.ButterKnife;

import static com.cinggl.cinggl.R.id.textView;
import static com.cinggl.cinggl.profile.UpdateProfileActivity.TAG;

public class HomeFragment extends Fragment{
    private static final String TAG = HomeFragment.class.getSimpleName();
    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String  EXTRA_USER_UID = "uid";
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private HomePagerAdapter homePagerAdapter;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mUid;
    private Handler handler = new Handler();
    private int progressStatus = 0;
    private ProgressBar progressBar;


    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance(int sectionNumber) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        firebaseAuth =  FirebaseAuth.getInstance();
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUid = firebaseUser.getUid();


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, view);

        homePagerAdapter = new HomePagerAdapter(getChildFragmentManager());
        tabLayout = (TabLayout)view.findViewById(R.id.tabs);
        viewPager = (ViewPager)view.findViewById(R.id.container);
        viewPager.setAdapter(homePagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_layout, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(id == R.id.action_profile){
            Intent intent = new Intent(getActivity(), PersonalProfileActivity.class);
            intent.putExtra(HomeFragment.EXTRA_USER_UID, mUid);
            startActivity(intent);

        }

        if (id == R.id.action_account_settings){
            Intent intent = new Intent(getActivity(), UpdateProfileActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }

//    // Method to manually check connection status
//    private void checkConnection() {
//        boolean isConnected = ConnectivityReceiver.isConnected();
//        showConnection(isConnected);
//    }
//
//    //Showing the status in Snackbar
//    private void showConnection(boolean isConnected) {
//        String message;
//        if (isConnected) {
//            mConnectonEstablishedTextView.setText("Connection established");
//
//            final Handler handler = new Handler();
//            Timer t = new Timer();
//            t.schedule(new TimerTask() {
//                public void run() {
//                    handler.post(new Runnable() {
//                        public void run() {
//                            mNetworkRelativeLayout.setVisibility(View.GONE);
//                        }
//                    });
//                }
//            }, 2000);
//
//        } else {
////            mNetworkRelativeLayout.setVisibility(View.VISIBLE);
////            mConnectonEstablishedTextView.setText("Disconnected");
//
//            new AlertDialog.Builder(getContext())
//                    .setTitle("No Internet Connection")
//                    .setMessage("It looks like your internet connection is off. Please turn it " +
//                            "on and try again")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
//        }
//
////        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
//
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//
//        // register connection status listener
//        App.getInstance().setConnectivityListener(this);
//        checkConnection();
//    }
//
//    /**
//     * Callback will be triggered when there is change in
//     * network connection
//     */
//    @Override
//    public void onNetworkConnectionChanged(boolean isConnected) {
//        showConnection(isConnected);
//    }
}

