package com.andeqa.andeqa.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.registration.SignInActivity;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    private CollectionReference usersReference;
    private CollectionReference followingCollection;
    private CollectionReference queryOptionsCollectons;
    private FirebaseAuth firebaseAuth;
    private static final String EXTRA_POST_ID = "post id";
    private static final String COLLECTION_ID = "collection id";
    private static final String TYPE = "type";
    private static final String EXTRA_USER_UID =  "uid";
    private CollectionReference allCollections;
    private boolean queryOptions = false;
    private boolean playServicesAvailable = true;
    private static final int REQUEST_PLAY_SERVICES_RESOLUTION = 9000;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String firstLaunch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //check for cases where google play services is not available
        // or needs to be updated
        playServicesAvailable = checkPlayServices();
        if (!playServicesAvailable){
            Toast.makeText(this,"play not services available", Toast.LENGTH_SHORT).show();
        }else {
            init();
        }

    }

    private void init(){
        allCollections = FirebaseFirestore.getInstance().collection(Constants.COLLECTIONS);
        followingCollection = FirebaseFirestore.getInstance().collection(Constants.PEOPLE_RELATIONS);
        queryOptionsCollectons = FirebaseFirestore.getInstance().collection(Constants.QUERY_OPTIONS);
        allCollections.orderBy("time").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (queryDocumentSnapshots.isEmpty()){
                    Log.d("collections are absent", queryDocumentSnapshots.size() + "");
                }else {
                    Log.d("collections are present", queryDocumentSnapshots.size() + "");
                }

            }
        });

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                authenticationListener();
            }
        }, 2000);
    }

    private boolean checkPlayServices(){
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(getApplicationContext());
        if (result != ConnectionResult.SUCCESS){
            if (googleApiAvailability.isUserResolvableError(result)){
                googleApiAvailability.getErrorDialog(this, result, REQUEST_PLAY_SERVICES_RESOLUTION)
                        .show();
            }
            return false;
        }
        return true;
    }

    private void authenticationListener(){
        firebaseAuth = FirebaseAuth.getInstance();
        queryOptions = true;
        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            usersReference.document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(final DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (e != null) {
                                Log.w(TAG, "Listen error", e);
                                return;
                            }

                            if (documentSnapshot.exists()){
                                queryOptionsCollectons.document("options")
                                        .collection(firebaseAuth.getCurrentUser().getUid())
                                        .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                    @Override
                                    public void onEvent(@Nullable QuerySnapshot documentSnapshots, @Nullable FirebaseFirestoreException e) {
                                        if (e != null) {
                                            Log.w(TAG, "Listen error", e);
                                            return;
                                        }

                                        if (queryOptions){
                                            if (documentSnapshots.isEmpty()){
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                                queryOptions = false;

                                            }else {
                                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();

                                                queryOptions = false;
                                            }
                                        }
                                    }
                                });

                            }else {

                                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }
                    });


        }else {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }


}
