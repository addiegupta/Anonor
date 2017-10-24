package com.example.android.talktime.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.example.android.talktime.R;

import butterknife.BindView;

public class WaitingCallActivity extends AppCompatActivity {

    @BindView(R.id.pb_calling_people)
    ProgressBar mPBCallingPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_call);

        Handler handler = new Handler();
        long delayInMilliseconds = 35000;
        handler.postDelayed(new Runnable() {
            public void run() {
                startActivity(new Intent(WaitingCallActivity.this, NoResponseActivity.class));
            }
        }, delayInMilliseconds);
    }
}

