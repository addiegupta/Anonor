package com.addie.xcall.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.addie.xcall.utils.NetworkUtil;

import timber.log.Timber;

/**
 * Sends an intent when network connection changes state.
 * Used to display alert dialog when internet connection is not available and stop the app
 */
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