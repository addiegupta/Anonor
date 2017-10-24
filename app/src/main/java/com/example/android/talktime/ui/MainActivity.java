package com.example.android.talktime.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.android.talktime.R;
import com.example.android.talktime.SinchService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sinch.android.rtc.SinchError;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements SinchService.StartFailedListener {


    private static final String IS_CALLER_KEY = "is_caller";
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FCM_TOKEN_KEY = "fcm_token";
    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String CALL_REQUEST_KEY = "call_request";

    @Nullable
    @BindView(R.id.btn_send_push)
    Button mSendPushButton;

    private FirebaseAuth mAuth;
    private FirebaseDatabase mUserDatabase;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDBRef;
    private String mReceiverId;
    private boolean mIsCaller;
    private String mFirebaseIDToken;
    private boolean mPendingCallRequest = false;
    private String mOriginalCaller;

//TODO Review and correct pendingcallrequest approach
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            handlePermissions();
        }

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mIsCaller = prefs.getBoolean(IS_CALLER_KEY, true);

        mAuth = FirebaseAuth.getInstance();
        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        getFirebaseIDToken();
        Timber.plant(new Timber.DebugTree());
        Timber.d("mIsCaller:" + String.valueOf(mIsCaller));

        if (mIsCaller) {
            setContentView(R.layout.activity_main_caller);
            ButterKnife.bind(this);
            mSendPushButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendCallRequest();
                }
            });

        } else {
            setContentView(R.layout.activity_main_receiver);
            ButterKnife.bind(this);

            if (getIntent().hasExtra(CALLERID_DATA_KEY)) {
                mOriginalCaller = getIntent().getStringExtra(CALLERID_DATA_KEY);
                mPendingCallRequest = true;

                Intent callScreenActivity = new Intent(this, CallScreenActivity.class);
                callScreenActivity.putExtra(CALLERID_DATA_KEY,mOriginalCaller);
                startActivity(callScreenActivity);
            }
        }
    }


    private void handlePermissions() {

        int permissionCheck = ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int menuId = item.getItemId();

        switch (menuId) {
            case R.id.menu_main_action_sign_out:
                showSignOutAlertDialog();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSignOutAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sign_out_confirmation);
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton(R.string.sign_out, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                signOutUser();
            }
        });
        builder.show();
    }


    private void sendCallRequest() {


//        update call status in Db
        mDBRef.child("callers").child(mAuth.getCurrentUser().getUid()).child(CALL_REQUEST_KEY).setValue("true");

        String url = "https://us-central1-talktime-5f9a9.cloudfunctions.net/sendPush";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Timber.d(error.toString());
            }

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", mFirebaseIDToken);
                return headers;
            }
        };
        Timber.d(stringRequest.toString());
        Volley.newRequestQueue(this).add(stringRequest);

        startActivity(new Intent(MainActivity.this, WaitingCallActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }

    private void signOutUser() {

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mIsCaller) {
                        mDBRef.child("callers").child(mAuth.getCurrentUser().getUid()).child(FCM_TOKEN_KEY).removeValue();
                    } else {
                        mDBRef.child("receivers").child(mAuth.getCurrentUser().getUid()).child(FCM_TOKEN_KEY).removeValue();

                    }
                    FirebaseInstanceId.getInstance().deleteInstanceId();
                    FirebaseInstanceId.getInstance().getToken();
                    Timber.d(FirebaseInstanceId.getInstance().getToken());
                    mAuth.signOut();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        finish();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }


    @Override
    public void onStartFailed(SinchError error) {
        Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show();
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
            getSinchServiceInterface().startClient(mAuth.getCurrentUser().getUid());
            Toast.makeText(MainActivity.this, "Registered as " + mAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
        }

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
