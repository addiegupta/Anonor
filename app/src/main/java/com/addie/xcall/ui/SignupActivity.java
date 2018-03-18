package com.addie.xcall.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.addie.xcall.R;
import com.addie.xcall.model.User;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignupActivity extends AppCompatActivity {
    private static final String FCM_TOKEN = "fcm_token";
    private static final String SINCH_ID_KEY = "sinch_id";

    @BindView(R.id.sign_up_button)
    Button mButtonSignup;

    private FirebaseDatabase mUserDatabase;
    private DatabaseReference mDBRef;
    private static final String SHARED_PREFS_KEY = "shared_prefs";
    private static final String FIRST_LOGIN = "first_login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        ButterKnife.bind(this);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        if (!prefs.contains(FIRST_LOGIN)) {
            prefs.edit().putBoolean(FIRST_LOGIN, false).apply();
        } else {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        }

        mUserDatabase = FirebaseDatabase.getInstance();
        mDBRef = mUserDatabase.getReference();

        mButtonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
                String fcmToken = prefs.getString(FCM_TOKEN, null);
                User user = new User(0, fcmToken);

                mDBRef.child("users").child(fcmToken).setValue(user);
                prefs.edit().putString(SINCH_ID_KEY, fcmToken.substring(0, 25)).apply();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}
