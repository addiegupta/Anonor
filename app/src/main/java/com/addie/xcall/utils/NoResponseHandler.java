package com.addie.xcall.utils;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.addie.xcall.ui.NoResponseActivity;

public class NoResponseHandler {

    private static Handler mNoResponseHandler;

    public static Handler getHandler(Context context) {
        if (mNoResponseHandler == null) {
            mNoResponseHandler = new Handler(Looper.getMainLooper());
        }
        initHandler(context);
        return mNoResponseHandler;
    }

    public static void initHandler(final Context context) {

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
