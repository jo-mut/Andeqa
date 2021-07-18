package com.andeka.andeka.settings;


import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.andeka.andeka.Constants;
import com.andeka.andeka.R;
import com.andeka.andeka.registration.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteAccountFragment extends DialogFragment implements View.OnClickListener{
    @Bind(R.id.emailEditText)EditText mEmailEditText;
    @Bind(R.id.passwordEditText)EditText mPasswordEditText;
    @Bind(R.id.noRelativeLayout)RelativeLayout mNoRelativeLayout;
    @Bind(R.id.YesRelativeLayout)RelativeLayout mYesRelativeLayout;

    private ProgressDialog progressDialog;
    private CollectionReference mUsersCollectionReference;
    private FirebaseAuth firebaseAuth;


    public static DeleteAccountFragment newInstance(String title){
        DeleteAccountFragment deleteAccountFragment = new DeleteAccountFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        deleteAccountFragment.setArguments(args);
        return deleteAccountFragment;

    }

    public DeleteAccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete_account, container, false);
        ButterKnife.bind(this, view);

        mYesRelativeLayout.setOnClickListener(this);
        mNoRelativeLayout.setOnClickListener(this);
        firebaseAuth = FirebaseAuth.getInstance();

        if (firebaseAuth.getCurrentUser() != null){
            mUsersCollectionReference = FirebaseFirestore.getInstance().collection(Constants.FIREBASE_USERS);
        }

        deleteAccountProgessDialog();

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Dialog dialog = getDialog();

        if (dialog != null){
            dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

    }


    public void deleteAccountProgessDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting your profile...");
        progressDialog.setCancelable(true);
        progressDialog.getWindow().setLayout(100, 150);
    }


    private void deleteAccount(){

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        if (!TextUtils.isEmpty(mEmailEditText.getText().toString()) &&
                !TextUtils.isEmpty(mPasswordEditText.getText().toString())){
            progressDialog.show();

            final String email = mEmailEditText.getText().toString().trim();
            final String password = mPasswordEditText.getText().toString().trim();

            final AuthCredential credential = EmailAuthProvider
                    .getCredential(email, password);

            mUsersCollectionReference.document(firebaseAuth.getCurrentUser().getUid())
                    .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                            if (documentSnapshot.exists()){
                                mUsersCollectionReference.document(firebaseAuth.getCurrentUser()
                                        .getUid()).delete();

                                user.reauthenticate(credential)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    user.delete();
                                                    Intent intent = new Intent(getContext(), SignUpActivity.class);
                                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                    startActivity(intent);

                                                    Toast.makeText(getContext(), "Account successfully deleted",
                                                            Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }else {
                                                    Toast.makeText(getContext(), "Check that email and password match and try again",
                                                            Toast.LENGTH_LONG).show();
                                                    progressDialog.dismiss();
                                                }

                                            }
                                        });


                            }
                        }
                    });


        }else {
            Toast.makeText(getContext(),"Email and password cannot be empty", Toast.LENGTH_SHORT).show();
        }


    }



    @Override
    public void onClick(View v){
        if (v == mNoRelativeLayout){
            dismiss();
        }

        if (v == mYesRelativeLayout){
            deleteAccount();
        }
    }

}
