package com.example.android.talktime.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.talktime.AudioPlayer;
import com.example.android.talktime.R;
import com.example.android.talktime.SinchService;
import com.example.android.talktime.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallEndCause;
import com.sinch.android.rtc.calling.CallListener;

import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

// Sinch code
public class CallScreenActivity extends BaseActivity {

    private AudioPlayer mAudioPlayer;
    private Timer mTimer;
    private UpdateCallDurationTask mDurationTask;
    private String CALLER_SCREEN_BOOL_KEY = "caller_screen";
    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String IS_CALLER_KEY = "is_caller";
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String DB_DURATION_KEY = "duration";
    private boolean mIsCaller;

    private String mCallId;

    @BindView(R.id.callDuration)
    TextView mCallDuration;
    @BindView(R.id.callState)
    TextView mCallState;
    @BindView(R.id.hangupButton)
    Button mEndCallButton;
    private boolean mPendingCallRequest = false;
    private String mOriginalCaller;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDBRef;
    private long mTotalDuration;

    private class UpdateCallDurationTask extends TimerTask {

        @Override
        public void run() {
            CallScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateCallDuration();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_screen);
        ButterKnife.bind(this);

        Timber.plant(new Timber.DebugTree());
        mAudioPlayer = new AudioPlayer(this);

        SharedPreferences preferences = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mIsCaller = preferences.getBoolean(IS_CALLER_KEY, true);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDBRef = mDatabase.getReference();

        if (getIntent().hasExtra(CALLERID_DATA_KEY)) {
            mOriginalCaller = getIntent().getStringExtra(CALLERID_DATA_KEY);
            mPendingCallRequest = true;
        } else {
            mCallId = getIntent().getStringExtra(SinchService.CALL_ID);
            Timber.d(mCallId);
        }

        mEndCallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCall();
            }
        });


        //Get duration value
        mDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                saveDuration(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

    }

    private void saveDuration(DataSnapshot snapshot) {
        DataSnapshot list;
        if (mIsCaller) {
            list = snapshot.child("callers");
        } else {
            list = snapshot.child("receivers");
        }
        for (DataSnapshot ds : list.getChildren()) {

            if (ds.getKey().equals(mAuth.getCurrentUser().getUid())) {
                mTotalDuration = ds.getValue(User.class).getDuration();
            }
        }
    }

    @Override
    public void onServiceConnected() {

        if (getSinchServiceInterface() != null && !getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mAuth.getCurrentUser().getUid());
            Toast.makeText(CallScreenActivity.this, "Registered as " + mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
        }
        if (mPendingCallRequest) {
            try {
                Call call = getSinchServiceInterface().callUser(mOriginalCaller);

                if (call == null) {
                    // Service failed for some reason, show a Toast and abort
                    Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                            + "placing a call.", Toast.LENGTH_LONG).show();
                    return;
                }
                mCallId = call.getCallId();
                call.addCallListener(new SinchCallListener());
                mCallState.setText(call.getState().toString());

            } catch (MissingPermissionException e) {
                ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
            }
        } else {

            Call call = getSinchServiceInterface().getCall(mCallId);
            if (call != null) {
                call.answer();
                call.addCallListener(new SinchCallListener());
                mCallState.setText(call.getState().toString());
            } else {
                Timber.e("Started with invalid callId, aborting.");
                finish();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mDurationTask.cancel();
        mTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTimer = new Timer();
        mDurationTask = new UpdateCallDurationTask();
        mTimer.schedule(mDurationTask, 0, 500);
    }

    @Override
    public void onBackPressed() {
        // User should exit activity by ending call, not by going back.
    }

    private void endCall() {
        mAudioPlayer.stopProgressTone();
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            call.hangup();
        }
        finish();
    }

    private String formatTimespan(int totalSeconds) {
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    private void updateCallDuration() {
        Call call = getSinchServiceInterface().getCall(mCallId);
        if (call != null) {
            mCallDuration.setText(formatTimespan(call.getDetails().getDuration()));
        }
    }

    private void updateDatabaseCallDuration(final long duration) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Looper.myLooper() == null) {
                    Looper.prepare();
                }
                mTotalDuration += duration;
                if (mIsCaller) {
                    mDBRef.child("callers").child(mAuth.getCurrentUser().getUid()).child(DB_DURATION_KEY).setValue(mTotalDuration);
                } else {
                    mDBRef.child("receivers").child(mAuth.getCurrentUser().getUid()).child(DB_DURATION_KEY).setValue(mTotalDuration);
                }
            }
        }).start();
    }

    private class SinchCallListener implements CallListener {

        @Override
        public void onCallEnded(Call call) {
            CallEndCause cause = call.getDetails().getEndCause();
            Timber.d("Call ended. Reason: " + cause.toString());
            mAudioPlayer.stopProgressTone();
            setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
            String endMsg = "Call ended: " + call.getDetails().toString();
            Toast.makeText(CallScreenActivity.this, endMsg, Toast.LENGTH_LONG).show();
            long duration = call.getDetails().getDuration();

            updateDatabaseCallDuration(duration);


            endCall();
        }

        @Override
        public void onCallEstablished(Call call) {
            Timber.d("Call established");
            mAudioPlayer.stopProgressTone();
            mCallState.setText(call.getState().toString());
            setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        }

        @Override
        public void onCallProgressing(Call call) {
            Timber.d("Call progressing");
            mAudioPlayer.playProgressTone();
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> pushPairs) {
            // Send a push through your push provider here, e.g. GCM


        }

    }
}
