package com.cinggl.cinggl.profile;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.home.NavigationDrawerActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ResetPasswordActivity extends AppCompatActivity implements
        View.OnClickListener{
    @Bind(R.id.btn_reset_password)Button mResetPasswordButton;
    @Bind(R.id.registeredEmailEditText)EditText mRegisteredEmailEditText;
    @Bind(R.id.progressBar)ProgressBar mProgressBar;
//    @Bind(R.id.btn_back)Button mBackButton;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);

        firebaseAuth = FirebaseAuth.getInstance();

        mResetPasswordButton.setOnClickListener(this);


    }

    @Override
    public void onClick(View v){
        if (v == mResetPasswordButton){
            String email = mRegisteredEmailEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email)){
                Toast.makeText(getApplication(), "Enter your registered email", Toast.LENGTH_LONG).show();
                return;
            }

            mProgressBar.setVisibility(View.VISIBLE);
            firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                Toast.makeText(ResetPasswordActivity.this, "Check your email! " +
                                        "We have sent instructions to resent your password!",
                                        Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email." +
                                        " Check that you entered your registered email",
                                        Toast.LENGTH_SHORT).show();
                            }
                            mProgressBar.setVisibility(View.GONE);
                        }
                    });

            Intent intent = new Intent(ResetPasswordActivity.this, NavigationDrawerActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();

        }

    }
}
