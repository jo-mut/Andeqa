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
import android.widget.Toast;

import com.andeqa.andeqa.Constants;
import com.andeqa.andeqa.R;
import com.andeqa.andeqa.main.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

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


    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private ProgressDialog mProgressDialog;
    private CollectionReference usersCollection;
    private GoogleSignInClient mGoogleSignInClient;
    private DatabaseReference usersReference;
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email";
    private static final int RC_SIGN_IN = 9001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        ButterKnife.bind(this);
        mRegisterTextView.setOnClickListener(this);
        mPasswordLoginButton.setOnClickListener(this);
        mForgotPasswordTextView.setOnClickListener(this);
        mGoogleSignInButton.setOnClickListener(this);
        mFirebaseAuth = FirebaseAuth.getInstance();
        createAuthProgressDialog();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull final FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
                    usersCollection.document(mFirebaseAuth.getCurrentUser().getUid())
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
        mFirebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }catch (ApiException e){
                Log.w(TAG, "google sign failed",  e);
                Toast.makeText(SignInActivity.this, "Sign in with Google failed please try once more", Toast.LENGTH_SHORT).show();
            }

        }

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
            loginWithGoogle();
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
        mProgressDialog.show();

        if (email.equals("")) {
            mEmailEditText.setError("Please enter your email");
            return;
        }

        if (password.equals("")) {
            mPasswordEditText.setError("Password cannot be blank");
            return;
        }

        mFirebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                mProgressDialog.dismiss();
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

                    usersCollection.document(mFirebaseAuth.getCurrentUser().getUid())
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

    private void loginWithGoogle() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setTitle("Please Wait");
        mProgressDialog.setCancelable(false);


        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d("LoginActivity", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("LoginActivity", "onAuthStateChanged:signed_out");
                }

            }
        };


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("firebase user account", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        try {
            mProgressDialog.show();
        } catch (Exception e) {

        }
        // [END_EXCLUDE]

        final AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.e(TAG, "sign in failed " + task.getException().getMessage());
                            Log.w("reason signin failed", "signInWithCredential", task.getException());
                            Toast.makeText(SignInActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else {
                            Log.d("sign in successful", "signInWithCredential:onComplete:" + task.isSuccessful());

                            usersCollection = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
                            usersCollection.document(mFirebaseAuth.getCurrentUser().getUid())
                                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                                            if (e != null) {
                                                Log.w(TAG, "Listen error", e);
                                                return;
                                            }

                                            if (documentSnapshot.exists()){
                                                Intent intent = new Intent(SignInActivity.this, HomeActivity.class);
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }else {
                                                Intent intent = new Intent(SignInActivity.this, CreateProfileActivity.class);
                                                intent.putExtra(SignInActivity.EMAIL, acct.getEmail());
                                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                                finish();
                                            }
                                        }
                                    });
                        }
                        // [START_EXCLUDE]

                        try {
                            mProgressDialog.dismiss();
                        } catch (Exception e) {

                        }
                        // [END_EXCLUDE]
                    }
                });
    }


    private void createAuthProgressDialog() {
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("Loading...");
        mProgressDialog.setMessage("Authenticating your sign in details...");
        mProgressDialog.setCancelable(false);
    }
}
