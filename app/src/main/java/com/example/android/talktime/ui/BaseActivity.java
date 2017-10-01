package com.example.android.talktime.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;

import com.example.android.talktime.CallService;

    // Sinch code
public abstract class BaseActivity extends AppCompatActivity implements ServiceConnection{

    private CallService.CallServiceInterface mCallServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationContext().bindService(new Intent(this, CallService.class), this,
                BIND_AUTO_CREATE);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        if (CallService.class.getName().equals(name.getClassName())) {
            mCallServiceInterface = (CallService.CallServiceInterface) iBinder;
            onServiceConnected();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        if (CallService.class.getName().equals(componentName.getClassName())) {
            mCallServiceInterface = null;
            onServiceDisconnected();
        }
    }

    protected void onServiceConnected() {
        // for subclasses
    }

    protected void onServiceDisconnected() {
        // for subclasses
    }

    protected CallService.CallServiceInterface getSinchServiceInterface() {
        return mCallServiceInterface;
    }
}
