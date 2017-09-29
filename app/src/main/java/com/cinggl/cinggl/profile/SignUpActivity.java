package com.cinggl.cinggl.profile;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cinggl.cinggl.Constants;
import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.cinggl.cinggl.ifair.SetCinglePriceActivity;
import com.cinggl.cinggl.models.Cingulan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SignUpActivity extends AppCompatActivity implements
        View.OnClickListener{
    public static final String TAG = SignUpActivity.class.getSimpleName();

    @Bind(R.id.createUserButton) Button mCreateUserButton;
    @Bind(R.id.emailEditText) EditText mEmailEditText;
    @Bind(R.id.passwordEditText) EditText mPasswordEditText;
    @Bind(R.id.confirmPasswordEditText) EditText mConfirmPasswordEditText;
    @Bind(R.id.loginTextView) TextView mLoginTextView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mAuthProgressDialog;
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
//    private ProgressDialog mLogingInProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();


        createAuthStateListener();
        createAuthProgressDialog();

        mLoginTextView.setOnClickListener(this);
        mCreateUserButton.setOnClickListener(this);
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

    @Override
    public void onClick(View view) {

        if (view == mLoginTextView) {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        if (view == mCreateUserButton) {
            createNewUser();

        }

    }

//    private void loginWithPassword() {
//        String email = mEmailEditText.getText().toString().trim();
//        String password = mPasswordEditText.getText().toString().trim();
//
//        if (email.equals("")) {
//            mEmailEditText.setError("Please enter your email");
//            return;
//        }
//
//        if (password.equals("")) {
//            mPasswordEditText.setError("Password cannot be blank");
//            return;
//        }
//
//        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
//            @Override
//            public void onComplete(@NonNull Task<AuthResult> task) {
//                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
//                mAuthProgressDialog.dismiss();
//                if (!task.isSuccessful()) {
//                    Log.w(TAG, "signInWithEmail", task.getException());
//                    Toast.makeText(SignUpActivity.this, "Please confirm that your email and password match",
//                            Toast.LENGTH_SHORT).show();
//                }else {
//                    checkIfImailVerified();
//                }
//            }
//        });
//
//    }
//
//
//
//    public void checkIfImailVerified(){
//        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
//        if (firebaseUser.isEmailVerified()){
//
//            //user is verified sp you can finish this activity or send user to activity you want
//            Toast.makeText(SignUpActivity.this, "You have Successfully signed in",
//                    Toast.LENGTH_SHORT).show();
//
//            Intent intent = new Intent(SignUpActivity.this, CreateProfileActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//            startActivity(intent);
//            finish();
//
//        }else {
//            //email is not verified so just prompt the massge to the user and restart this activity
//            FirebaseAuth.getInstance().signOut();
//            //restart this activity
//
//            new AlertDialog.Builder(SignUpActivity.this)
//                    .setTitle("Sorry !")
//                    .setMessage("Please make sure that you have verified your email so you can sign in")
//                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                        }
//                    }).setIcon(android.R.drawable.ic_dialog_alert).show();
//
//
//            overridePendingTransition(0,0);
//            finish();
//            overridePendingTransition(0,0);
//            startActivity(getIntent());
//        }
//    }

    private void createNewUser() {
        //editText for email and password
        final String email = mEmailEditText.getText().toString().trim();
        final String password = mPasswordEditText.getText().toString().trim();
        String confirmPassword = mConfirmPasswordEditText.getText().toString().trim();

        //validation for email and password
        boolean validEmail = isValidEmail(email);
        boolean validPassword = isValidPassword(password, confirmPassword);
        if (!validEmail || !validPassword) return;

        mAuthProgressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mAuthProgressDialog.dismiss();
                        if (!task.isSuccessful()) {
                            //check email exists
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                new AlertDialog.Builder(SignUpActivity.this)
                                        .setTitle("Sorry !")
                                        .setMessage("User with this email already exists. Please choose another email!")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }else {
                                //sign up failed
                                new AlertDialog.Builder(SignUpActivity.this)
                                        .setTitle("Authentication failed")
                                        .setMessage("Check that you are connection to the internet")
                                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                            }
                                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
                            }
                        }else {
                            //sign up successful
                            Log.d(TAG, "Authentication successful");
                            if (mAuth.getCurrentUser().getUid() != null){
                                Intent intent = new Intent(SignUpActivity.this, CreateProfileActivity.class);
                                intent.putExtra(SignUpActivity.EMAIL, email);
                                intent.putExtra(SignUpActivity.PASSWORD, password);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();

                            }
                        }

                    }

                });
    }


    private void createAuthStateListener() {
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    sendVerificationEmail();
                }else {
                    //user has not created an account
                }
            }
        };

    }


    private void sendVerificationEmail(){
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        firebaseUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    //email sent
                    Toast.makeText(SignUpActivity.this, "Confirm your email! Verification email successfully sent to" + " " +
                            firebaseUser.getEmail(), Toast.LENGTH_LONG).show();
                    //after email is sent, sign out and finish this activity
                    FirebaseAuth.getInstance().signOut();
//                    Intent intent = new Intent(SignUpActivity.this, CreateProfileActivity.class);
//                    startActivity(intent);
//                    finish();
                }else {
                    //email not sent, so display a message and restart the activity and restart this activity
                    Toast.makeText(SignUpActivity.this, "Could not send verification email", Toast.LENGTH_LONG).show();
                    new AlertDialog.Builder(SignUpActivity.this)
                            .setMessage("Cinggl could not send you verification email, please confirm that you " +
                                    "entered the right email and check your internet connection")
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).setIcon(android.R.drawable.ic_dialog_alert).show();
                    overridePendingTransition(0,0);
                    finish();
                    overridePendingTransition(0,0);
                    startActivity(getIntent());
                }
            }
        });
    }

    private void createAuthProgressDialog() {
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle("Loading...");
        mAuthProgressDialog.setMessage("Authenticating your sign up details...");
        mAuthProgressDialog.setCancelable(false);
    }


    private boolean isValidEmail(String email) {
        boolean isGoodEmail =
                (email != null && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEmailEditText.setError("Please enter a valid email address");
            return false;
        }
        return isGoodEmail;
    }



    private boolean isValidPassword(String password, String confirmPassword) {
        if (password.length() < 6) {
            mPasswordEditText.setError("Please create a password containing at least 6 characters");
            return false;
        } else if (!password.equals(confirmPassword)) {
            mPasswordEditText.setError("Passwords do not match");
            return false;
        }
        return true;
    }

}
