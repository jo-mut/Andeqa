package com.cinggl.cinggl.profile;

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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.MainActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

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

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;
    private CollectionReference usersRefernece;
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
        mAuth = FirebaseAuth.getInstance();
        createAuthProgressDialog();

        usersRefernece = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
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
                    Log.w(TAG, "signInWithEmail", task.getException());
                    mErrorRelativeLayout.setVisibility(View.VISIBLE);
                    mErrorTextView.setText("Please confirm that your email and password match and " +
                            "that you are connected to the internet");
                    mAuthProgressDialog.dismiss();
                }else {
                    checkIfImailVerified();
                }
            }
        });

    }

    private void checkIfImailVerified(){
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser.isEmailVerified()){

            if (usersRefernece.document(mAuth.getCurrentUser().getUid()) != null){
                //LAUCNH SETUP PROFIFLE ACTIVITY IF NO
                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }else {
                navigateToCreateProfile();
            }

            //user is verified sp you can finish this activity or send user to activity you want
            Toast.makeText(SignInActivity.this, "You have Successfully signed in",
                    Toast.LENGTH_SHORT).show();

        }else {
            //email is not verified so just prompt the massge to the user and restart this activity
            FirebaseAuth.getInstance().signOut();
            //restart this activity

            mErrorRelativeLayout.setVisibility(View.VISIBLE);
            mErrorTextView.setText("Check that you have confirmed your email");
            overridePendingTransition(0,0);
            finish();
            overridePendingTransition(0,0);
            startActivity(getIntent());
        }
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
            }else if (TextUtils.isEmpty(mPasswordEditText.getText())){
                mErrorRelativeLayout.setVisibility(View.VISIBLE);
                mErrorTextView.setText("Password cannot be empty!");
            }else {
                loginWithPassword();
            }
        }

        if (v == mForgotPasswordTextView){
            Intent intent = new Intent(SignInActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        }

    }


}
