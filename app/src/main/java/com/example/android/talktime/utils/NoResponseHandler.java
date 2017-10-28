package com.example.android.talktime.utils;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.example.android.talktime.ui.NoResponseActivity;

public class NoResponseHandler {

    private static Handler mNoResponseHandler;

    public static Handler getHandler(Context context) {
        if (mNoResponseHandler == null) {
            initHandler(context);
        }
        return mNoResponseHandler;
    }

    public static void initHandler(final Context context) {
        mNoResponseHandler = new Handler(Looper.getMainLooper());

        mNoResponseHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                context.startActivity(new Intent(context, NoResponseActivity.class));

            }
        }, 35000);
    }

    public static void stopHandler() {
        mNoResponseHandler.removeCallbacksAndMessages(null);
    }

}
