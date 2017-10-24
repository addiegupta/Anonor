package com.example.android.talktime.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.android.talktime.R;
import com.example.android.talktime.utils.CustomUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NoResponseActivity extends AppCompatActivity {


    private static final String CALL_REQUEST_KEY = "call_request";


    private FirebaseAuth mAuth;
    private FirebaseDatabase mUserDatabase;
    private DatabaseReference mDBRef;
    private String mFirebaseIDToken;


    @BindView(R.id.btn_try_again_call)
    Button mTryAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_response);
        ButterKnife.bind(this);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        mDBRef.child("callers").child(mAuth.getCurrentUser().getUid()).child(CALL_REQUEST_KEY).setValue("false");

        getFirebaseIDToken();
        mTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryCalllingAgain();
            }
        });

    }

    private void tryCalllingAgain() {

        String callerId = mAuth.getCurrentUser().getUid();
        CustomUtils.sendCallRequest(this, callerId, mDBRef, mFirebaseIDToken);
    }
    /**
     * Needed for authentication of user while using cloud functions
     */
    private void getFirebaseIDToken() {
        mAuth.getCurrentUser().getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            mFirebaseIDToken = task.getResult().getToken();
                        } else {
                            // Handle error -> task.getException();
                            task.getException().printStackTrace();
                        }
                    }
                });
    }


}
