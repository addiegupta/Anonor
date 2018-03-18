package com.addie.xcall.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.addie.xcall.R;
import com.addie.xcall.receivers.NetworkChangeReceiver;
import com.addie.xcall.services.SinchService;
import com.addie.xcall.utils.CustomUtils;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.sinch.android.rtc.SinchError;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends BaseActivity implements SinchService.StartFailedListener {


    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FCM_TOKEN_KEY = "fcm_token";
    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String SINCH_ID_KEY = "sinch_id";


    private FirebaseDatabase mUserDatabase;
    private DatabaseReference mDBRef;
    private String mFcmToken;
    private String mSinchId;
    private String mOriginalCaller;
    private NetworkChangeReceiver networkChangeReceiver;
    private BroadcastReceiver mDialogReceiver;
    private AlertDialog mInternetDialog;

    @BindView(R.id.fab_main_call)
    FloatingActionButton mCallFAB;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            handlePermissions();
        }

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


        Timber.plant(new Timber.DebugTree());

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        mCallFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCallRequest();
            }
        });


        if (getIntent().hasExtra(CALLERID_DATA_KEY)) {
            mOriginalCaller = getIntent().getStringExtra(CALLERID_DATA_KEY);
            Timber.d("Original caller is :" + mOriginalCaller);

            // Start CallScreenActivity
            Intent callScreenActivity = new Intent(this, CallScreenActivity.class);
            callScreenActivity.putExtra(CALLERID_DATA_KEY, mOriginalCaller);
            startActivity(callScreenActivity);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        createDialogReceiver();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent.getBooleanExtra("EXIT", false)) {
            finish();
        }
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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

    private void handlePermissions() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mDialogReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Timber.uprootAll();
        unregisterReceiver(networkChangeReceiver);

    }


    private void sendCallRequest() {

        CustomUtils.sendCallRequest(this, mFcmToken, mDBRef);
    }

    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, "An error has occured.Please restart the app", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStarted() {
    }

    @Override
    protected void onServiceConnected() {

        Timber.d("onServiceConnected");
        getSinchServiceInterface().setStartListener(this);

        //Register user
        if (getSinchServiceInterface() != null && !getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mSinchId);
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));

    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission succesfully granted", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Cannot function without microphone access", Toast
                    .LENGTH_LONG).show();
            finish();
        }
    }

}
