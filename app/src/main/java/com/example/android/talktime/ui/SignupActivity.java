package com.example.android.talktime.ui;

import android.content.Intent;
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
import com.example.android.talktime.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.BindView;

public class SignupActivity extends AppCompatActivity {

//TODO Refactor UI elements code using butterknife etc

    @BindView(R.id.sign_in_button)
    Button mButtonSignin;
    @BindView(R.id.sign_up_button)
    Button mButtonSignup;
    @BindView(R.id.et_email)
    EditText mETEmail;
    @BindView(R.id.et_password)
    EditText mETPassword;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mPBLoadingIndicator;
    @BindView(R.id.btn_reset_password)
    Button mButtonResetPass;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mUserDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDBRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        mButtonResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, ResetPasswordActivity.class));
            }
        });

        mButtonSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = mETEmail.getText().toString().trim();
                String password = mETPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplicationContext(), getString(R.string.enter_email_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getApplicationContext(),getString(R.string.enter_pass_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.length() < 6) {
                    Toast.makeText(getApplicationContext(), getString(R.string.minimum_password), Toast.LENGTH_SHORT).show();
                    return;
                }

                mPBLoadingIndicator.setVisibility(View.VISIBLE);

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

                //create user
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(SignupActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                Toast.makeText(SignupActivity.this, "createUserWithEmail:onComplete:" + task.isSuccessful(), Toast.LENGTH_SHORT).show();
                                mPBLoadingIndicator.setVisibility(View.GONE);
                                // If sign in fails, display a message to the user. If sign in succeeds
                                // the mAuth state listener will be notified and logic to handle the
                                // signed in user can be handled in the listener.
                                if (!task.isSuccessful()) {
                                    Toast.makeText(SignupActivity.this, "Authentication failed." + task.getException(),
                                            Toast.LENGTH_SHORT).show();
                                } else {

                                    //TODO Update for recievers as well

                                    User user = new User(email,0);
                                    String uniqueUserId = mAuth.getCurrentUser().getUid();
                                    mDBRef.child("callers").child(uniqueUserId).setValue(user);

                                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                                    finish();
                                }
                            }
                        });

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mPBLoadingIndicator.setVisibility(View.GONE);
    }
}