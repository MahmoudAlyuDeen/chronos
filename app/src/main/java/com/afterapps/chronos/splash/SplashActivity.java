package com.afterapps.chronos.splash;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.afterapps.chronos.Constants;
import com.afterapps.chronos.R;
import com.afterapps.chronos.home.HomeActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/*
 * Created by Mahmoud on 10/6/2016.
 */

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    final String uid = user.getUid();
                    navigateHome(uid);
                } else {
                    mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                final String uid = task.getResult().getUser().getUid();
                                initializeNode(uid);
                                navigateHome(uid);
                            } else {
                                Toast.makeText(SplashActivity.this, R.string.generic_error_connection, Toast.LENGTH_LONG).show();
                                finish();
                            }
                        }
                    });
                }
            }
        };
    }

    private void initializeNode(String uid) {
        FirebaseDatabase.getInstance()
                .getReference()
                .child(uid)
                .setValue(uid);
    }

    private void navigateHome(String uid) {
        final Intent home = new Intent(SplashActivity.this, HomeActivity.class);
        home.putExtra(Constants.UID_EXTRA, uid);
        startActivity(home);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
}