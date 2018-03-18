package com.addie.xcall.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.addie.xcall.R;
import com.addie.xcall.receivers.NetworkChangeReceiver;
import com.addie.xcall.utils.CustomUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NoResponseActivity extends AppCompatActivity {


    private static final String CALL_REQUEST_KEY = "call_request";


    private FirebaseDatabase mUserDatabase;
    private DatabaseReference mDBRef;
    private NetworkChangeReceiver networkChangeReceiver;
    private BroadcastReceiver mDialogReceiver;
    private AlertDialog mInternetDialog;

    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FCM_TOKEN_KEY = "fcm_token";
    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String SINCH_ID_KEY = "sinch_id";


    private String mFcmToken;
    private String mSinchId;


    @BindView(R.id.fab_no_response_call)
    FloatingActionButton mTryAgainButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_response);
        ButterKnife.bind(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("ACTION_TRIGGER_RECEIVER");

        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, filter);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mFcmToken = prefs.getString(FCM_TOKEN_KEY, null);
        mSinchId = prefs.getString(SINCH_ID_KEY, null);


        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        mDBRef.child("users").child(mFcmToken).child(CALL_REQUEST_KEY).setValue("false");

        mTryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tryCalllingAgain();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        createDialogReceiver();

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    private void createDialogReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("ACTION_NO_INTERNET");
        filter.addAction("ACTION_INTERNET_AVAILABLE");

        mDialogReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("ACTION_NO_INTERNET")) {

                    if (mInternetDialog == null || !mInternetDialog.isShowing()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(NoResponseActivity.this);
                        builder.setTitle(R.string.no_internet)
                                .setMessage("The app cannot function without an internet connection")
                                .setCancelable(false)
                                .setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent("ACTION_TRIGGER_RECEIVER");
                                        sendBroadcast(intent);
                                    }
                                })
                                .setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        intent.putExtra("EXIT", true);
                                        startActivity(intent);
                                    }
                                });
                        mInternetDialog = builder.create();
                        mInternetDialog.show();
                    }
                } else {
                    if (mInternetDialog != null && mInternetDialog.isShowing()) {
                        mInternetDialog.dismiss();
                        Toast.makeText(context, "Internet access restored", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(mDialogReceiver, filter);
    }

    private void tryCalllingAgain() {

        CustomUtils.sendCallRequest(this, mSinchId, mDBRef);
        finish();
    }



    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDialogReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);

    }
}
