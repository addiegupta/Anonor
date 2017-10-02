package com.example.android.talktime.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.android.talktime.CallService;
import com.example.android.talktime.R;
import com.example.android.talktime.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements CallService.StartFailedListener {


    private static final String IS_CALLER_KEY = "is_caller";
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    @Nullable
    @BindView(R.id.btn_main_call_someone)
    Button mButtonCallSomeone;


    private FirebaseAuth mAuth;
    private FirebaseDatabase mUserDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDBRef;
    private String mReceiverId;
    private boolean mIsCaller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mIsCaller = prefs.getBoolean(IS_CALLER_KEY, true);
        Timber.d("mIsCaller:" + String.valueOf(mIsCaller));
        if (mIsCaller) {
            setContentView(R.layout.activity_main_caller);
            ButterKnife.bind(this);
            mButtonCallSomeone.setEnabled(false);

            mButtonCallSomeone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callSomeone();
                }
            });
        } else {
            setContentView(R.layout.activity_main_receiver);
            ButterKnife.bind(this);
        }


        Timber.plant(new Timber.DebugTree());


        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        // Read from the database
        mDBRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                if (mIsCaller) {
                    selectCaller(dataSnapshot);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


        if (getSinchServiceInterface() != null && !getSinchServiceInterface().isStarted()) {
            String userName = mAuth.getCurrentUser().getEmail();
            getSinchServiceInterface().startClient(userName);
        }
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
        startCall();
    }


    private void selectCaller(DataSnapshot dataSnapshot) {

        DataSnapshot list = dataSnapshot.child("receivers");
        int dbSize = (int) list.getChildrenCount();

        //TODO Optimise for memory
        Random r = new Random();
        int ranNum = r.nextInt(dbSize);
        User userToBeCalled;
        int i = 0;
        for (DataSnapshot ds : list.getChildren()) {
            if (ranNum == i) {
                userToBeCalled = ds.getValue(User.class);
                mReceiverId = userToBeCalled.getEmail();
                mButtonCallSomeone.setEnabled(true);
                Toast.makeText(this, mReceiverId, Toast.LENGTH_SHORT).show();
                break;
            }
            i++;
        }
    }

    private void callSomeone() {

        if (!getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mReceiverId);
        } else {
            startCall();
        }
    }

    private void startCall() {
        try {
            Call call = getSinchServiceInterface().callUser(mReceiverId);

            String callId = call.getCallId();
            Intent callScreen = new Intent(this, CallScreenActivity.class);
            callScreen.putExtra(CallService.CALL_ID, callId);
            startActivity(callScreen);
        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }
    }


    @Override
    protected void onServiceConnected() {

        getSinchServiceInterface().setStartListener(this);
    }


    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You may now place a call", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "This application needs permission to use your microphone to function properly.", Toast
                    .LENGTH_LONG).show();
        }
    }

}
