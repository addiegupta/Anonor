package com.example.android.talktime.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ProgressBar;

import com.example.android.talktime.R;
import com.example.android.talktime.utils.NoResponseHandler;

import butterknife.BindView;

public class WaitingCallActivity extends AppCompatActivity {

    @BindView(R.id.pb_calling_people)
    ProgressBar mPBCallingPeople;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_call);

        NoResponseHandler.getHandler(this);
    }
}

