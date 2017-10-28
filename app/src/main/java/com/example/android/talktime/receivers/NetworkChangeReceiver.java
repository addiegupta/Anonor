package com.example.android.talktime.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.android.talktime.utils.NetworkUtil;

import timber.log.Timber;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        int status = NetworkUtil.getConnectivityStatusString(context);
            if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                Timber.d("No network");

                Intent noInternetIntent = new Intent("ACTION_NO_INTERNET");
                context.sendBroadcast(noInternetIntent);
            } else if (status == NetworkUtil.NETWORK_STATUS_WIFI || status== NetworkUtil.NETWORK_STATUS_MOBILE){
                Timber.d("Network available");
                Intent internetAvailableIntent = new Intent("ACTION_INTERNET_AVAILABLE");
                context.sendBroadcast(internetAvailableIntent);
            }

    }
}