package com.addie.xcall.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.addie.xcall.R;
import com.addie.xcall.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Logs the user into database by saving its fcm token
 */
public class InitActivity extends AppCompatActivity {
    private static final String FCM_TOKEN = "fcm_token";
    private static final String SINCH_ID_KEY = "sinch_id";

    @BindView(R.id.sign_up_button)
    Button mButtonSignup;
    @BindView(R.id.pb_loading_indicator)
    ProgressBar mLoadingIndicator;

    private FirebaseDatabase mUserDatabase;
    private DatabaseReference mDBRef;
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FIRST_LOGIN = "first_login";
    private TokenReceiver mTokenReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);

        ButterKnife.bind(this);

        IntentFilter filter = new IntentFilter("com.addie.xcall.token");
        mTokenReceiver = new TokenReceiver();
        registerReceiver(mTokenReceiver,filter);

        mLoadingIndicator.setVisibility(View.VISIBLE);
        mButtonSignup.setVisibility(View.GONE);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        if (prefs.contains(FIRST_LOGIN)) {
            startActivity(new Intent(InitActivity.this, MainActivity.class));
            finish();
        }
        if(prefs.contains(FCM_TOKEN)){
            mButtonSignup.setVisibility(View.VISIBLE);
            mLoadingIndicator.setVisibility(View.GONE);
        }

        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                prefs.edit().putBoolean(FIRST_LOGIN, false).apply();
                String fcmToken = prefs.getString(FCM_TOKEN, null);
                User user = new User(0, fcmToken, "false");

                mDBRef.child("users").child(fcmToken).setValue(user);
                prefs.edit().putString(SINCH_ID_KEY, fcmToken.substring(0, 25)).apply();
                startActivity(new Intent(InitActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mTokenReceiver);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    class TokenReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            mLoadingIndicator.setVisibility(View.GONE);
            mButtonSignup.setVisibility(View.VISIBLE);

        }
    }
}


