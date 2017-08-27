package com.cinggl.cinggl.ifair;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.cinggl.cinggl.leasing.LeasingActivity;
import com.cinggl.cinggl.models.Ifair;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChooseTradeMethodActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    //BIND VIEWS
    @Bind(R.id.cingleBackingRadioButton)RadioButton mCingleBackingRadioButton;
    @Bind(R.id.cingleLacingRadioButton)RadioButton mCingleLacingRadioButton;
    @Bind(R.id.cingleLeasingRadioButton)RadioButton mCingleLeasingRadioButton;
    @Bind(R.id.cingleSellingRadioButton)RadioButton mCingleSellingRadioButton;

    @Bind(R.id.toolbar)Toolbar toolbar;

    private static final String EXTRA_POST_KEY = "post key";
    private DatabaseReference ifairReference;
    private DatabaseReference cinglesReference;
    private String mPostKey;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_trade_method);
        ButterKnife.bind(this);

        //BACK NAVIGATION
        setSupportActionBar(toolbar);
        navigateBack();

        firebaseAuth = FirebaseAuth.getInstance();

        mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
        if(mPostKey == null){
            throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
        }

        //initialize radio button and attach a click handler;
        mCingleSellingRadioButton.setOnCheckedChangeListener(this);
        mCingleBackingRadioButton.setOnCheckedChangeListener(this);
        mCingleLacingRadioButton.setOnCheckedChangeListener(this);
        mCingleLeasingRadioButton.setOnCheckedChangeListener(this);

        //database references
        ifairReference = FirebaseDatabase.getInstance().getReference(Constants.IFAIR);
        cinglesReference = FirebaseDatabase.getInstance().getReference(Constants.FIREBASE_CINGLES);

    }

    private void navigateBack(){
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked){
        if (isChecked){
            if (compoundButton.getId() == R.id.cingleSellingRadioButton){
                //set other choice false
                mCingleBackingRadioButton.setChecked(false);
                mCingleLeasingRadioButton.setChecked(false);
                mCingleLacingRadioButton.setChecked(false);

                final String CingleSelling = mCingleSellingRadioButton.getText().toString();

                cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String image = (String) dataSnapshot.child("cingleImageUrl").getValue();
                        final String title = (String) dataSnapshot.child("title").getValue();
                        final String description = (String) dataSnapshot.child("description").getValue();

                        Ifair ifair =  new Ifair();
                        ifair.setCreator(firebaseAuth.getCurrentUser().getUid());
                        ifair.setTitle(title);
                        ifair.setDescription(description);
                        ifair.setImage(image);
                        ifair.setPushId(mPostKey);
                        ifairReference.child(CingleSelling).child(mPostKey).setValue(ifair);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            if (compoundButton.getId() == R.id.cingleLacingRadioButton){
                //set other choices false
                mCingleLeasingRadioButton.setChecked(false);
                mCingleBackingRadioButton.setChecked(false);
                mCingleSellingRadioButton.setChecked(false);

                final String CingleLacing = mCingleLacingRadioButton.getText().toString();

                cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String image = (String) dataSnapshot.child("cingleImageUrl").getValue();
                        final String title = (String) dataSnapshot.child("title").getValue();
                        final String description = (String) dataSnapshot.child("description").getValue();

                        Ifair ifair =  new Ifair();
                        ifair.setCreator(firebaseAuth.getCurrentUser().getUid());
                        ifair.setTitle(title);
                        ifair.setDescription(description);
                        ifair.setImage(image);
                        ifair.setPushId(mPostKey);
                        ifairReference.child(CingleLacing).child(mPostKey).setValue(ifair);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            if (compoundButton.getId() == R.id.cingleLeasingRadioButton){
                //set other choices false
                mCingleBackingRadioButton.setChecked(false);
                mCingleLacingRadioButton.setChecked(false);
                mCingleSellingRadioButton.setChecked(false);

                final String CingleLeasing = mCingleLeasingRadioButton.getText().toString();
                cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String image = (String) dataSnapshot.child("cingleImageUrl").getValue();
                        final String title = (String) dataSnapshot.child("title").getValue();
                        final String description = (String) dataSnapshot.child("description").getValue();

                        Ifair ifair =  new Ifair();
                        ifair.setCreator(firebaseAuth.getCurrentUser().getUid());
                        ifair.setTitle(title);
                        ifair.setDescription(description);
                        ifair.setImage(image);
                        ifair.setPushId(mPostKey);
                        ifairReference.child(CingleLeasing).child(mPostKey).setValue(ifair);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            if (compoundButton.getId() == R.id.cingleBackingRadioButton){
                //set other choices false
                mCingleLeasingRadioButton.setChecked(false);
                mCingleLacingRadioButton.setChecked(false);
                mCingleSellingRadioButton.setChecked(false);

                final String CingleBacking = mCingleBackingRadioButton.getText().toString();
                cinglesReference.child(mPostKey).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String image = (String) dataSnapshot.child("cingleImageUrl").getValue();
                        final String title = (String) dataSnapshot.child("title").getValue();
                        final String description = (String) dataSnapshot.child("description").getValue();

                        Ifair ifair =  new Ifair();
                        ifair.setCreator(firebaseAuth.getCurrentUser().getUid());
                        ifair.setTitle(title);
                        ifair.setDescription(description);
                        ifair.setImage(image);
                        ifair.setPushId(mPostKey);
                        ifairReference.child(CingleBacking).child(mPostKey).setValue(ifair);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        }
    }
}
