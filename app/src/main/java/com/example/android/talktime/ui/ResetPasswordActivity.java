package com.example.android.talktime.ui;

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
import com.google.firebase.auth.FirebaseAuth;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ResetPasswordActivity extends AppCompatActivity {

    @BindView(R.id.et_email)
    EditText mETEmail;
    @BindView(R.id.btn_reset_password)
    Button mButtonResetPass;
    @BindView(R.id.btn_back)
    Button mButtonBack;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mPBLoadingIndicator;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        ButterKnife.bind(this);
        Timber.plant(new Timber.DebugTree());

        auth = FirebaseAuth.getInstance();

        mButtonBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mButtonResetPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mETEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(),getString(R.string.enter_registered_email_toast), Toast.LENGTH_SHORT).show();
                    return;
                }

                mPBLoadingIndicator.setVisibility(View.VISIBLE);
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, getString(R.string.pass_reset_instructions_sent_toast), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this,getString(R.string.failed_send_reset_pass_email_pass), Toast.LENGTH_SHORT).show();
                                }

                                mPBLoadingIndicator.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }
}
