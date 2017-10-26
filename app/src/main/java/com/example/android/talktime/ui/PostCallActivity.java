package com.example.android.talktime.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
    private boolean mIsCaller;

    private static final String IS_CALLER_KEY = "is_caller";
    private static final String SHARED_PREFS_KEY = "shared_prefs";


    @BindView(R.id.btn_dismiss)
    Button mDismissButton;
    @BindView(R.id.btn_submit_problem)
    Button mReportUserButton;
    @BindView(R.id.et_post_call_problem)
    EditText mProblemEditText;
    @BindView(R.id.checkbox_report_user)
    CheckBox mReportUserCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_call);
        ButterKnife.bind(this);

        initialiseDatabase();

        mIsCaller = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE).getBoolean(IS_CALLER_KEY, true);
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
                submitProblem();
            }
        });
    }

    private void initialiseDatabase() {
        //Get Firebase mAuth instance
        mAuth = FirebaseAuth.getInstance();
        mDBRef = FirebaseDatabase.getInstance().getReference();
    }

    private void dismissActivity() {
        startActivity(new Intent(PostCallActivity.this, MainActivity.class));
        finish();
    }

    private void submitProblem() {

        String uniqueUserId = mAuth.getCurrentUser().getUid();
        String problemDescription = mProblemEditText.getText().toString().trim();
        boolean reportUser = mReportUserCheckbox.isChecked();
        Report callerReport = new Report(mRemoteUser, problemDescription, reportUser);
        if (mIsCaller) {

            mDBRef.child("problems").child("callers").child(uniqueUserId).push().setValue(callerReport);
        } else {
            mDBRef.child("problems").child("receivers").child(uniqueUserId).push().setValue(callerReport);

        }
        Toast.makeText(this, "Problem Submitted", Toast.LENGTH_SHORT).show();
        dismissActivity();

    }
}
