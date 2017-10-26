package com.example.android.talktime.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.android.talktime.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AppCompatActivity {


    private static final String FCM_TOKEN_KEY = "fcm_token";
    @BindView(R.id.et_email)
    EditText mETEmail;
    @BindView(R.id.et_password)
    EditText mETPassword;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mPBLoadingIndicator;
    @BindView(R.id.btn_signup)
    Button mButtonSignUp;
    @BindView(R.id.btn_login)
    Button mButtonLogin;
    @BindView(R.id.btn_reset_password)
    Button mResetPassButton;

    private FirebaseAuth auth;

    private FirebaseDatabase mUserDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDBRef;
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String IS_CALLER_KEY = "is_caller";
    private boolean mIsCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //Get Firebase auth instance
        auth = FirebaseAuth.getInstance();


        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        // set the view now
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // Read from the database
        mDBRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


        mButtonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(LoginActivity.this, SignupActivity.class));
            }
        });

        mResetPassButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ResetPasswordActivity.class));
            }
        });

        mButtonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                login();
            }
        });
    }

    private void login() {

        final String email = mETEmail.getText().toString();
        final String password = mETPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_email_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(), getString(R.string.enter_pass_toast),
                    Toast.LENGTH_SHORT).show();
            return;
        }

        mPBLoadingIndicator.setVisibility(View.VISIBLE);

        //authenticate user
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            mPBLoadingIndicator.setVisibility(View.GONE);
                            // there was an error
                            if (password.length() < 6) {
                                mETPassword.setError(getString(R.string.minimum_password));
                            } else {
                                Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();
                            }
                        } else {

                            Query query = mDBRef.child("callers");
                            query.addListenerForSingleValueEvent
                                    (new ValueEventListener() {
                                         @Override
                                         public void onDataChange(DataSnapshot dataSnapshot) {
                                             new CheckUserTask().execute(dataSnapshot);
                                         }

                                         @Override
                                         public void onCancelled(DatabaseError databaseError) {
                                         }
                                     }
                                    );
                        }
                    }
                });
    }

    private class CheckUserTask extends AsyncTask<DataSnapshot, Void, Void> {
        @Override
        protected Void doInBackground(final DataSnapshot... snapshots) {

            String userId = auth.getCurrentUser().getUid();
            DataSnapshot dataSnapshot = snapshots[0];
            mIsCaller = false;
            SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            String fcmToken = prefs.getString(FCM_TOKEN_KEY, null);
            if (dataSnapshot.exists()) {
                // dataSnapshot is the "callers" node with all children with email equal to user's email
                for (DataSnapshot caller : dataSnapshot.getChildren()) {
                    if (caller.getKey().equals(userId)) {
                        mIsCaller = true;
                        mDBRef.child("callers").child(userId).child(FCM_TOKEN_KEY).setValue(fcmToken);
                    }
                }
                if (!mIsCaller) {
                    mDBRef.child("receivers").child(userId).child(FCM_TOKEN_KEY).setValue(fcmToken);
                }

                prefs.edit().putBoolean(IS_CALLER_KEY, mIsCaller).apply();

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mPBLoadingIndicator.setVisibility(View.GONE);

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
