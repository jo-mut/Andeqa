package com.cinggl.cinggl.home;


;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.cinggl.cinggl.R;
import com.cinggl.cinggl.profile.SignUpActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class DeleteAccountDialog extends DialogFragment implements View.OnClickListener {
    @Bind(R.id.confirmDeleteRelativeLayout)RelativeLayout mConfirmRelativeLayout;
    private static final String TAG = DeleteAccountDialog.class.getSimpleName();
    private ProgressDialog progressDialog;

    public DeleteAccountDialog() {
        // Required empty public constructor
    }

    public static DeleteAccountDialog newInstance(String title){
        DeleteAccountDialog deleteAccountDialog = new DeleteAccountDialog();
        Bundle args = new Bundle();
        args.putString("title", title);
        deleteAccountDialog.setArguments(args);
        return deleteAccountDialog;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_delete_account_dialog, container, false);
        ButterKnife.bind(this, view);

        deleteAccountProgressDialog();

        mConfirmRelativeLayout.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        String title = getArguments().getString("title", "create your cingle");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onClick(View v){
        if (v == mConfirmRelativeLayout){
            deleteAccount();
        }
    }

    public void deleteAccountProgressDialog(){
        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Deleting your account...");
        progressDialog.setCancelable(false);
    }

    private void deleteAccount(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog.show();
        // Get auth credentials from the user for re-authentication. The example below shows
        // email and password credentials but there are multiple possible providers,
        // such as GoogleAuthProvider or FacebookAuthProvider.
        AuthCredential credential = EmailAuthProvider
                .getCredential("user@example.com", "password1234");

        // Prompt the user to re-provide their sign-in credentials
        try {
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            user.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "User account deleted.");
                                                progressDialog.dismiss();
                                            }
                                        }
                                    });

                        }
                    });
        }catch (Exception e){
            Toast.makeText(getContext(), "Sorry! You dont have an active account.Create a new account", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(getActivity(), SignUpActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }


}
