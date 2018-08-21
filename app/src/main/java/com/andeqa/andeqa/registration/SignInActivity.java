package com.andeqa.andeqa.registration;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.main.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SignInActivity extends AppCompatActivity implements
        View.OnClickListener{

    public static final String TAG = SignInActivity.class.getSimpleName();

    @Bind(R.id.passwordLoginButton)
    Button mPasswordLoginButton;
    @Bind(R.id.emailEditText)
    EditText mEmailEditText;
    @Bind(R.id.passwordEditText) EditText mPasswordEditText;
    @Bind(R.id.registerTextView)
    TextView mRegisterTextView;
    @Bind(R.id.forgotPasswordTextView)TextView mForgotPasswordTextView;
    @Bind(R.id.errorRelativeLayout)RelativeLayout mErrorRelativeLayout;
    @Bind(R.id.errorTextView)TextView mErrorTextView;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
    @Bind(R.id.googleSignInButton)Button mGoogleSignInButton;


    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;
    private CollectionReference usersCollection;
    private DatabaseReference usersReference;
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);
        mRegisterTextView.setOnClickListener(this);
        mPasswordLoginButton.setOnClickListener(this);
        mForgotPasswordTextView.setOnClickListener(this);
        mGoogleSignInButton.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        createAuthProgressDialog();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
                    usersCollection.document(mAuth.getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){
                                Log.d("user snapshot", documentSnapshot.toString());
                                //LAUCNH SETUP PROFIFLE ACTIVITY IF NO
                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else {
                                Log.d("user snapshot", "dociment snapshot does not exist");
                                navigateToCreateProfile();
                            }
                        }
                    });

                }
            }

        };
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }


    private void navigateToCreateProfile(){
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();

        if (email.equals("")) {
            mEmailEditText.setError("Please enter your email");
            return;
        }

        if (password.equals("")) {
            mPasswordEditText.setError("Password cannot be blank");
            return;
        }


        Intent intent = new Intent(SignInActivity.this, CreateProfileActivity.class);
        intent.putExtra(SignInActivity.PASSWORD, password);
        intent.putExtra(SignInActivity.EMAIL, email);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

    }

    private void loginWithPassword() {
        String email = mEmailEditText.getText().toString().trim();
        String password = mPasswordEditText.getText().toString().trim();
        int index = email.indexOf('@');
        String identity = email.substring(0, index);
        Log.d("identity", identity);
        mAuthProgressDialog.show();

        if (email.equals("")) {
            mEmailEditText.setError("Please enter your email");
            return;
        }

        if (password.equals("")) {
            mPasswordEditText.setError("Password cannot be blank");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                mAuthProgressDialog.dismiss();
                if (!task.isSuccessful()) {
                    Log.e(TAG, "sign in failed: " + task.getException().getMessage());

                    mErrorRelativeLayout.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("Please confirm that your email and password match and" + " " +
                            "that you are connected to the internet");
                    mErrorRelativeLayout.postDelayed(new Runnable() {
                        public void run() {
                            mErrorRelativeLayout.setVisibility(View.GONE);

                        }
                    }, 5000);


                }else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

                    /**get the device tokem id*/
//                    final String deviceId = FirebaseInstanceId.getInstance().getToken();

                    usersCollection.document(mAuth.getCurrentUser().getUid())
                            .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()){

                                //LAUCNH SETUP PROFIFLE ACTIVITY IF NO
                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            }else {
                                navigateToCreateProfile();
                            }
                        }
                    });
                }
            }
        });

    }

    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Authenticating your sign in details...");
        mAuthProgressDialog.setCancelable(false);
    }


    @Override
    public void onClick(View v) {

        if (v == mRegisterTextView) {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        }

        if (v == mPasswordLoginButton) {
            if (TextUtils.isEmpty(mEmailEditText.getText())){
                mErrorRelativeLayout.setVisibility(View.VISIBLE);
                mErrorTextView.setText("Email cannot be empty!");
                mErrorRelativeLayout.postDelayed(new Runnable() {
                    public void run() {
                        mErrorRelativeLayout.setVisibility(View.GONE);
                    }
                }, 5000);

            }else if (TextUtils.isEmpty(mPasswordEditText.getText())){
                mErrorRelativeLayout.setVisibility(View.VISIBLE);
                mErrorTextView.setText("Password cannot be empty!");
                mErrorRelativeLayout.postDelayed(new Runnable() {
                    public void run() {
                        mErrorRelativeLayout.setVisibility(View.GONE);
                    }
                }, 5000);


            }else {
                loginWithPassword();

            }
        }

        if (v == mForgotPasswordTextView){
            Intent intent = new Intent(SignInActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        }

        if (v == mGoogleSignInButton){
            Intent intent = new Intent(SignInActivity.this, SignInWithGoogle.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

    }

}
