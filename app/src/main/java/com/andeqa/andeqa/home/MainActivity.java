package com.andeqa.andeqa.home;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.registration.SignInActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private CollectionReference usersReference;
    private FirebaseAuth firebaseAuth;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.appNameTextView)TextView mAppNameTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        authenticationListener();
        setFonts();
    }


    private void authenticationListener(){
        firebaseAuth = FirebaseAuth.getInstance();
        mProgressBar.setVisibility(View.VISIBLE);
        if (firebaseAuth.getCurrentUser() != null){
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            Log.d("user is present", "user is present");
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            usersReference.document(firebaseAuth.getCurrentUser().getUid())
                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot documentSnapshot) {
                    if (documentSnapshot.exists()){
                        Log.d("user snapshot", documentSnapshot.toString());
                        Intent intent = new Intent(MainActivity.this, NavigationDrawerActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }else {

                        Log.d("user snapshot", documentSnapshot.toString());
                        Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();

                    }
                }
            });

        }else {
            Log.d("user is null", "user is present");
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setFonts(){
        Typeface appNameFont = Typeface.createFromAsset(getAssets(),
                "fonts/Lucida Handwriting Italic.ttf");
        mAppNameTextView.setTypeface(appNameFont);
    }
}
