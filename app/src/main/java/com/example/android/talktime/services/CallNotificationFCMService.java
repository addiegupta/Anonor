package com.example.android.talktime.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.android.talktime.R;
import com.example.android.talktime.ui.CallScreenActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class CallNotificationFCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String NOTIF_TITLE_TEXT = "Someone is calling";
    private static final String NOTIF_BODY_TEXT = "Tap to pick up";


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String callerId = remoteMessage.getData().get(CALLERID_DATA_KEY);
            Log.d(TAG, "Receiving call from: " + callerId);

            createNotification(callerId);
        }
    }

    private void createNotification(String callerId) {


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setSmallIcon(R.mipmap.ic_launcher_round)
                        .setContentTitle(NOTIF_TITLE_TEXT)
                        .setContentText(NOTIF_BODY_TEXT);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(CallNotificationFCMService.this, CallScreenActivity.class);
        intent.putExtra(CALLERID_DATA_KEY,callerId);

        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notificationBuilder.setContentIntent(pending);

        // using the same tag and Id causes the new notification to replace an existing one
        String tag = String.valueOf(callerId);
        mNotificationManager.notify(tag,1, notificationBuilder.build());

        removeNotification(tag);
    }

    private void removeNotification(final String tag) {
        Handler handler = new Handler(Looper.getMainLooper());
        long delayInMilliseconds = 30000;
        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        handler.postDelayed(new Runnable() {
            public void run() {
                notificationManager.cancel(tag,1);
            }
        }, delayInMilliseconds);
    }
}
