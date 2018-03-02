package com.andeqa.andeqa.market;

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

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.models.Single;
import com.andeqa.andeqa.utils.ProportionalImageView;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.models.PostSale;
import com.andeqa.andeqa.models.Cinggulan;
import com.andeqa.andeqa.models.Credit;
import com.andeqa.andeqa.profile.PersonalProfileActivity;
import com.andeqa.andeqa.people.FollowerProfileActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ListOnMarketActivity extends AppCompatActivity implements View.OnClickListener{
    @Bind(R.id.postImageView)ProportionalImageView mCingleImageView;
    @Bind(R.id.usernameTextView)TextView mAccountUsernameTextView;
    @Bind(R.id.profileImageView)CircleImageView mUserProfileImageView;
    @Bind(R.id.titleTextView)TextView mCingleTitleTextView;
    @Bind(R.id.cingleTitleRelativeLayout)RelativeLayout mCingleTitleRelativeLayout;
    @Bind(R.id.postSalePriceTextView)TextView mCingleSalePriceTextView;
    @Bind(R.id.setCinglePriceButton)Button mSetCinglePriceButton;
    @Bind(R.id.setCingleSalePriceEditText)EditText mSetCingleSalePriceEditText;

    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private String mPostKey;
    private static final String EXTRA_POST_KEY = "post key";
    private static final String EXTRA_USER_UID = "uid";
    private static final String TAG = ListOnMarketActivity.class.getSimpleName();
    //firestore
    private CollectionReference cinglesReference;
    private CollectionReference usersReference;
    private CollectionReference relationsReference;
    private CollectionReference commentReference;
    private CollectionReference senseCreditReference;
    private CollectionReference selllingCollection;



    //REMOVE SCIENTIFIC NOATATION
    private DecimalFormat formatter =  new DecimalFormat("0.00000000");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_on_market);
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
        FirebaseFirestore.setLoggingEnabled(true);

        if (firebaseAuth.getCurrentUser() != null){

            mPostKey = getIntent().getStringExtra(EXTRA_POST_KEY);
            if(mPostKey == null){
                throw new IllegalArgumentException("pass an EXTRA_POST_KEY");
            }

            cinglesReference = FirebaseFirestore.getInstance().collection(Constants.POSTS);
            relationsReference = FirebaseFirestore.getInstance().collection(Constants.RELATIONS);
            usersReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
            commentReference = FirebaseFirestore.getInstance().collection(Constants.COMMENTS);
            senseCreditReference = FirebaseFirestore.getInstance().collection(Constants.SENSECREDITS);
            selllingCollection = FirebaseFirestore.getInstance().collection(Constants.SELLING);

            //initialize input filter
            setEditTextFilter();
            //initialize click listeners
            mSetCinglePriceButton.setOnClickListener(this);

            setData();
        }
    }

    public void setData(){
        //set the cingle image
        cinglesReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (e != null) {
                    Log.w(TAG, "Listen error", e);
                    return;
                }

                if (documentSnapshot.exists()){
                    final Single single = documentSnapshot.toObject(Single.class);
                    final String uid = single.getUid();
                    final String title = single.getTitle();
                    final String image = single.getImage();

                    if (single.getTitle().equals("")){
                        mCingleTitleRelativeLayout.setVisibility(View.GONE);
                    }else {
                        mCingleTitleTextView.setText(single.getTitle());
                    }

                    //set the single title
                    mCingleTitleTextView.setText(title);
                    //set the single image
                    Picasso.with(ListOnMarketActivity.this)
                            .load(image)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(mCingleImageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ListOnMarketActivity.this)
                                            .load(image)
                                            .into(mCingleImageView);
                                }
                            });

                    //lauch the user profile
                    mUserProfileImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (uid.equals(firebaseAuth.getCurrentUser().getUid())){
                                Intent intent = new Intent(ListOnMarketActivity.this, PersonalProfileActivity.class);
                                startActivity(intent);
                            }else {
                                Intent intent = new Intent(ListOnMarketActivity.this, FollowerProfileActivity.class);
                                intent.putExtra(ListOnMarketActivity.EXTRA_USER_UID, uid);
                                startActivity(intent);
                            }
                        }
                    });

                    usersReference.document(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()){
                                final Cinggulan cinggulan = documentSnapshot.toObject(Cinggulan.class);
                                final String username = cinggulan.getUsername();
                                final String profileImage = cinggulan.getProfileImage();

                                mAccountUsernameTextView.setText(username);
                                Picasso.with(ListOnMarketActivity.this)
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
                                                Picasso.with(ListOnMarketActivity.this)
                                                        .load(profileImage)
                                                        .fit()
                                                        .centerCrop()
                                                        .placeholder(R.drawable.profle_image_background)
                                                        .into(mUserProfileImageView);
                                            }
                                        });

//

                            }
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
                final String formattedString = formatter.format(intSalePrice);

                senseCreditReference.document(mPostKey).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                        if (e != null) {
                            Log.w(TAG, "Listen error", e);
                            return;
                        }

                        if (documentSnapshot.exists()){
                            final Credit credit = documentSnapshot.toObject(Credit.class);
                            final double senseCredits = credit.getAmount();

                            if (intSalePrice < senseCredits){
                                mSetCingleSalePriceEditText.setError("Sale price is less than Single Sense Credit!");
                            }else if (intSalePrice >= senseCredits){
                                //SET CINGLE ON SALE IN SELLING
                                final PostSale postSale =  new PostSale();
                                postSale.setUid(firebaseAuth.getCurrentUser().getUid());
                                postSale.setPushId(mPostKey);
                                postSale.setSalePrice(intSalePrice);

                                selllingCollection.document(mPostKey).set(postSale).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            Toast.makeText(ListOnMarketActivity.this, "Your post has been listed on Ifair",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            }else {

                                new AlertDialog.Builder(ListOnMarketActivity.this)
                                        .setTitle("Sorry !")
                                        .setMessage("The sale price cannot be less than the Single's Sense Credit")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        }else {
                            //SET CINGLE ON SALE IN SELLING
                            final PostSale postSale =  new PostSale();
                            postSale.setUid(firebaseAuth.getCurrentUser().getUid());
                            postSale.setPushId(mPostKey);
                            postSale.setSalePrice(intSalePrice);

                            selllingCollection.document(mPostKey).set(postSale).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Toast.makeText(ListOnMarketActivity.this, "Your post has been listed for sale",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

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
