package com.example.android.talktime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.android.talktime.R;
import com.example.android.talktime.model.Report;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PostCallActivity extends AppCompatActivity {

    private static final String CALLERID_DATA_KEY = "callerId";
    private String mRemoteUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDBRef;

    @BindView(R.id.btn_dismiss)
    Button mDismissButton;
    @BindView(R.id.btn_report_user)
    Button mReportUserButton;
    @BindView(R.id.et_post_call_problem)
    EditText mProblemEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_call);
        ButterKnife.bind(this);

        initialiseDatabase();

        mRemoteUser = getIntent().getStringExtra(CALLERID_DATA_KEY);
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissActivity();
            }
        });
        mReportUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportUser();
            }
        });
    }

    private void initialiseDatabase(){
        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mDBRef = FirebaseDatabase.getInstance().getReference();
    }
    private void dismissActivity(){
        startActivity(new Intent(PostCallActivity.this,MainActivity.class));
        finish();
    }

    private void reportUser(){

        String uniqueUserId = mAuth.getCurrentUser().getUid();
        String problemDescription = mProblemEditText.getText().toString().trim();
        Report callerReport = new Report(mRemoteUser,problemDescription);
        mDBRef.child("reports").child(uniqueUserId).setValue(callerReport);
        Toast.makeText(this, "Reported User", Toast.LENGTH_SHORT).show();
        dismissActivity();

    }
}
