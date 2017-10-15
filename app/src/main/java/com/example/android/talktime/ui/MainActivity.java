package com.example.android.talktime.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
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
import com.example.android.talktime.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.sinch.android.rtc.MissingPermissionException;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class MainActivity extends BaseActivity implements SinchService.StartFailedListener {


    private static final String IS_CALLER_KEY = "is_caller";
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FCM_TOKEN_KEY = "fcm_token";
    private static final String CALLERID_DATA_KEY = "callerId";
    @Nullable
    @BindView(R.id.btn_main_call_someone)
    Button mButtonCallSomeone;
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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
            mButtonCallSomeone.setEnabled(false);
            mSendPushButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendPush();
                }
            });
            mButtonCallSomeone.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callSomeone();
                }
            });

        } else {
            setContentView(R.layout.activity_main_receiver);
            ButterKnife.bind(this);

            if (getIntent().hasExtra(CALLERID_DATA_KEY)){
                String callerId = getIntent().getStringExtra(CALLERID_DATA_KEY);

            }

        }

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

    }

    private void getFirebaseIDToken() {
        mAuth.getCurrentUser().getToken(true)
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            mFirebaseIDToken = task.getResult().getToken();
                            // Send token to your backend via HTTPS
                            // ...
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


    private void sendPush() {


        String url = "https://us-central1-talktime-5f9a9.cloudfunctions.net/sendPush";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d("DDF", response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("DDF", error.toString());
            }

        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", mFirebaseIDToken);
                return headers;
            }
        };
        Volley.newRequestQueue(this).add(stringRequest);
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

    private void selectCaller(DataSnapshot dataSnapshot) {

        DataSnapshot list = dataSnapshot.child("receivers");

        applyOldAlgorithm(list);
//        applyNewAlgorithm(list);

    }

    private void applyOldAlgorithm(DataSnapshot list) {

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

    private void applyNewAlgorithm(DataSnapshot list) {

        int dbSize = (int) list.getChildrenCount();

        int usersToBeCalled = Math.min(dbSize / 10 + 1, 10);


    }

    private void callSomeone() {
        try {
            Call call = getSinchServiceInterface().callUser(mReceiverId);

            if (call == null) {
                // Service failed for some reason, show a Toast and abort
                Toast.makeText(this, "Service is not started. Try stopping the service and starting it again before "
                        + "placing a call.", Toast.LENGTH_LONG).show();
                return;
            }
            String callId = call.getCallId();
            Intent callScreenActivity = new Intent(this, CallScreenActivity.class);
            callScreenActivity.putExtra(SinchService.CALL_ID, callId);
            startActivity(callScreenActivity);

        } catch (MissingPermissionException e) {
            ActivityCompat.requestPermissions(this, new String[]{e.getRequiredPermission()}, 0);
        }
    }


    @Override
    protected void onServiceConnected() {

        Timber.d("onServiceConnected");
        getSinchServiceInterface().setStartListener(this);

        //Register user
        if (getSinchServiceInterface() != null && !getSinchServiceInterface().isStarted()) {
            getSinchServiceInterface().startClient(mAuth.getCurrentUser().getEmail());
            Toast.makeText(MainActivity.this, "Registered as " + mAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();
        }
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
