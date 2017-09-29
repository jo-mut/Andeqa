package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;

import static android.os.Build.VERSION_CODES.N;

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

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;

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

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //check if the user has created personal profile
                    Intent intent = new Intent(SignInActivity.this, NavigationDrawerActivity.class);
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
                    Toast.makeText(SignInActivity.this, "Please confirm that your email and password match",
                            Toast.LENGTH_SHORT).show();
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

            //user is verified sp you can finish this activity or send user to activity you want
            Toast.makeText(SignInActivity.this, "You have Successfully signed in",
                    Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignInActivity.this, NavigationDrawerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }else {
            //email is not verified so just prompt the massge to the user and restart this activity
            FirebaseAuth.getInstance().signOut();
            //restart this activity

            Toast.makeText(SignInActivity.this, "Check that you have confirmed your email",
                    Toast.LENGTH_SHORT).show();

            overridePendingTransition(0,0);
            finish();
            overridePendingTransition(0,0);
            startActivity(getIntent());
        }
    }

    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Checking your sign in details...");
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
            loginWithPassword();
        }

        if (v == mForgotPasswordTextView){
            Intent intent = new Intent(SignInActivity.this, ResetPasswordActivity.class);
            startActivity(intent);
            finish();
        }

    }


}
