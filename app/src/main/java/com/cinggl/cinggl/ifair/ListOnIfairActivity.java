package com.cinggl.cinggl.ifair;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.ProportionalImageView;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.models.Cingle;
import com.cinggl.cinggl.models.CingleSale;
import com.cinggl.cinggl.models.Cingulan;
import com.cinggl.cinggl.profile.PersonalProfileActivity;
import com.cinggl.cinggl.people.FollowerProfileActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListOnIfairActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.cingleImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.creatorImageView)CircleImageView mUserProfileImageView;
    @Bind(R.id.cingleTitleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.cingleSalePriceTextView)TextView mCingleSalePriceTextView;
    @Bind(R.id.setCinglePriceButton)Button mSetCinglePriceButton;
    @Bind(R.id.setCingleSalePriceEditText)EditText mSetCingleSalePriceEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = ListOnIfairActivity.class.getSimpleName();
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference usersReference;
    private CollectionReference ifairReference;
    private CollectionReference relationsReference;
    private CollectionReference commentReference;
    //REMOVE SCIENTIFIC NOATATION
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_on_ifair);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null){

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            commentReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            ifairReference = FirebaseFirestore.getInstance().collection(Constants.IFAIR);


            //initialize input filter
            setEditTextFilter();
            //initialize click listeners
            mSetCinglePriceButton.setOnClickListener(this);

            setData();
        }
    }

    public void setData(){
        //SET THE CINGLE IMAGE AND USER PROFILE
        cinglesReference.document("Cingles").collection("Cingles")
                .document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Cingle cingle = documentSnapshot.toObject(Cingle.class);
                    final String uid = cingle.getUid();
                    final String title = cingle.getTitle();
                    final String image = cingle.getCingleImageUrl();


                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                final Cingulan cingulan = documentSnapshot.toObject(Cingulan.class);
                                final String username = cingulan.getUsername();
                                final String profileImage = cingulan.getProfileImage();

                                mAccountUsernameTextView.setText(username);
                                Picasso.with(ListOnIfairActivity.this)
                                        .load(profileImage)
                                        .fit()
                                        .centerCrop()
                                        .placeholder(R.drawable.profle_image_background)
                                        .networkPolicy(NetworkPolicy.OFFLINE)
                                        .into(mUserProfileImageView, new Callback() {
                                            @Override
                                            public void onSuccess() {

                                            }

                                            @Override
                                            public void onError() {
                                                Picasso.with(ListOnIfairActivity.this)
                                                        .load(profileImage)
                                                        .fit()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(mUserProfileImageView);
                                            }
                                        });

//                              LAUCNH PROFILE IF ITS NOT DELETED ELSE CATCH THE EXCEPTION
                                mUserProfileImageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                            Intent intent = new Intent(ListOnIfairActivity.this, PersonalProfileActivity.class);
                                            startActivity(intent);
                                        }else {
                                            Intent intent = new Intent(ListOnIfairActivity.this, FollowerProfileActivity.class);
                                            intent.putExtra(ListOnIfairActivity.EXTRA_USER_UID, uid);
                                            startActivity(intent);
                                        }
                                    }
                                });

                            }
                        }
                    });

                    //set the title of the cingle
                    if (title.equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(title);
                    }

                    //set the cingle image
                    Picasso.with(ListOnIfairActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ListOnIfairActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
                                }
                            });


                }

            }
        });

    }


    @Override
    public void onClick(View v){
        if (v == mSetCinglePriceButton){
            //GET EDITTEXT INPUT
            final String stringSalePrice = mSetCingleSalePriceEditText.getText().toString().trim();
            if (stringSalePrice.equals("")){
                mSetCingleSalePriceEditText.setError("Sale price is empty!");
            }else {
                final double intSalePrice = Double.parseDouble(stringSalePrice);
                Log.d("amount entered", intSalePrice + "");
                final String formattedString = formatter.format(intSalePrice);

                cinglesReference.document("Cingles").collection("Cingles").document(mPostKey)
                        .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Cingle cingle = documentSnapshot.toObject(Cingle.class);
                            final Double senseCredits = cingle.getSensepoint();
                            Log.d("seanse credits", senseCredits + "");

                            if (intSalePrice < senseCredits){
                                mSetCingleSalePriceEditText.setError("Sale price is less than Cingle Sense Credits!");
                            }else if (intSalePrice >= senseCredits){
                                //SET CINGLE ON SALE IN IFAIR
                                final CingleSale cingleSale =  new CingleSale();
                                cingleSale.setUid(firebaseAuth.getCurrentUser().getUid());
                                cingleSale.setPushId(mPostKey);
                                cingleSale.setSalePrice(intSalePrice);
                                Log.d("set sale price", intSalePrice + "");

                                ifairReference.document("Cingles").collection("Cingle Selling")
                                        .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot documentSnapshots) {

                                        if (!documentSnapshots.isEmpty()){

                                            final int index = documentSnapshots.size();
                                            Log.d("cingle count", index + "");

                                            final Long timeStamp = System.currentTimeMillis();

                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                                            String date = simpleDateFormat.format(new Date());

                                            if (date.endsWith("1") && !date.endsWith("11"))
                                                simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
                                            else if (date.endsWith("2") && !date.endsWith("12"))
                                                simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
                                            else if (date.endsWith("3") && !date.endsWith("13"))
                                                simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
                                            else
                                                simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
                                            String currentDate = simpleDateFormat.format(new Date());

                                            final long currentIdex = index + 1;
                                            Log.d("current index", currentIdex + "");


                                            cingleSale.setDatePosted(currentDate);
                                            cingleSale.setRandomNumber(currentIdex);
                                            cingleSale.setTimeStamp(timeStamp);
                                            cingleSale.setNumber(currentIdex);
                                            cingleSale.setRandomNumber((double) new Random().nextDouble());

                                            ifairReference.document("Cingles").collection("Cingle Selling").document(mPostKey)
                                                    .set(cingleSale).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ListOnIfairActivity.this, "Your cingle has been listed on Ifair",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    new AlertDialog.Builder(ListOnIfairActivity.this)
                                                            .setTitle("Sorry !")
                                                            .setMessage("Looks like something went wrong. Please try again later!")
                                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                }
                                                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                                }
                                            });
                                        }else {
                                            final int index = 0;
                                            Log.d("cingle count", index + "");

                                            Cingle cingle = new Cingle();

                                            final Long timeStamp = System.currentTimeMillis();

                                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("d");
                                            String date = simpleDateFormat.format(new Date());

                                            if (date.endsWith("1") && !date.endsWith("11"))
                                                simpleDateFormat = new SimpleDateFormat("d'st' MMM yyyy");
                                            else if (date.endsWith("2") && !date.endsWith("12"))
                                                simpleDateFormat = new SimpleDateFormat("d'nd' MMM yyyy");
                                            else if (date.endsWith("3") && !date.endsWith("13"))
                                                simpleDateFormat = new SimpleDateFormat("d'rd' MMM yyyy");
                                            else
                                                simpleDateFormat = new SimpleDateFormat("d'th' MMM yyyy");
                                            String currentDate = simpleDateFormat.format(new Date());

                                            final long currentIdex = index + 1;
                                            Log.d("current index", currentIdex + "");


                                            cingleSale.setDatePosted(currentDate);
                                            cingleSale.setRandomNumber(currentIdex);
                                            cingleSale.setTimeStamp(timeStamp);

                                            ifairReference.document("Cingles").collection("Cingle Selling").document(mPostKey)
                                                    .set(cingleSale).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(ListOnIfairActivity.this, "Your cingle has been listed on Ifair",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    new AlertDialog.Builder(ListOnIfairActivity.this)
                                                            .setTitle("Sorry !")
                                                            .setMessage("Looks like something went wrong. Please try again later!")
                                                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                }
                                                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                                                }
                                            });
                                        }

                                    }
                                });

                            }else {

                                new AlertDialog.Builder(ListOnIfairActivity.this)
                                        .setTitle("Sorry !")
                                        .setMessage("The sale price cannot be less than the Cingle's Sense Credits")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        }
                    }
                });

                mSetCingleSalePriceEditText.setText("");
            }

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    public void onStop(){
        super.onStop();

    }

    public void setEditTextFilter(){
        mSetCingleSalePriceEditText.setFilters(new InputFilter[] {
                new DigitsKeyListener(Boolean.FALSE, Boolean.TRUE) {
                    int beforeDecimal = 13, afterDecimal = 8;

                    @Override
                    public CharSequence filter(CharSequence source, int start, int end,
                                               Spanned dest, int dstart, int dend) {
                        String temp = mSetCingleSalePriceEditText.getText() + source.toString();

                        if (temp.equals(".")) {
                            return "0.";
                        }else if (temp.equals("0")){
                            return "0.";//if number begins with 0 return decimal place right after
                        }else if (temp.toString().indexOf(".") == -1) {
                            // no decimal point placed yet
                            if (temp.length() > beforeDecimal) {
                                return "";
                            }
                        } else {
                            temp = temp.substring(temp.indexOf(".") + 1);
                            if (temp.length() > afterDecimal) {
                                return "";
                            }
                        }

                        return super.filter(source, start, end, dest, dstart, dend);
                    }
                }
        });

    }
}
