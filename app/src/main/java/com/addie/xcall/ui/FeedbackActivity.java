package com.addie.xcall.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.addie.xcall.R;
import com.addie.xcall.model.Report;
import com.addie.xcall.receivers.NetworkChangeReceiver;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


/**
 * Displayed after the call. Provides option to submit feedback
 */
public class FeedbackActivity extends AppCompatActivity {

    private static final String CALLERID_DATA_KEY = "callerId";
    private String mRemoteUser;
    private DatabaseReference mDBRef;
    private NetworkChangeReceiver networkChangeReceiver;
    private BroadcastReceiver mDialogReceiver;
    private AlertDialog mInternetDialog;



    private static final String FCM_TOKEN_KEY = "fcm_token";
    private static final String SINCH_ID_KEY = "sinch_id";


    private String mFcmToken;
    private String mSinchId;

    private static final String SHARED_PREFS_KEY = "shared_prefs";


    @BindView(R.id.btn_dismiss)
    Button mDismissButton;
    @BindView(R.id.btn_submit_problem)
    Button mReportUserButton;
    @BindView(R.id.et_post_call_problem)
    EditText mProblemEditText;
    @BindView(R.id.checkbox_report_user)
    CheckBox mReportUserCheckbox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ButterKnife.bind(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
        filter.addAction("ACTION_TRIGGER_RECEIVER");

        networkChangeReceiver = new NetworkChangeReceiver();
        registerReceiver(networkChangeReceiver, filter);


        initialiseDatabase();

        mRemoteUser = getIntent().getStringExtra(CALLERID_DATA_KEY);
        Timber.d("Got remote user " + mRemoteUser);
        mDismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissActivity();
            }
        });
        mReportUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProblem();
            }
        });
    }

    private void initialiseDatabase() {
        //Get Firebase mAuth instance
        mDBRef = FirebaseDatabase.getInstance().getReference();
    }

    private void dismissActivity() {
        startActivity(new Intent(FeedbackActivity.this, MainActivity.class));
        finish();
    }
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

                        AlertDialog.Builder builder = new AlertDialog.Builder(FeedbackActivity.this);
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
                    if (mInternetDialog != null && mInternetDialog.isShowing() ) {
                        mInternetDialog.dismiss();
                        Toast.makeText(context, "Internet access restored", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        registerReceiver(mDialogReceiver, filter);
    }

    private void submitProblem() {
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        mFcmToken = prefs.getString(FCM_TOKEN_KEY, null);
        mSinchId = prefs.getString(SINCH_ID_KEY, null);

        String problemDescription = mProblemEditText.getText().toString().trim();
        boolean reportUser = mReportUserCheckbox.isChecked();
        Report callerReport = new Report(mRemoteUser, problemDescription, reportUser);

            mDBRef.child("problems").child(mFcmToken).push().setValue(callerReport);

        Toast.makeText(this, "Problem Submitted", Toast.LENGTH_SHORT).show();
        dismissActivity();

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
