package com.addie.xcall.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.addie.xcall.R;
import com.addie.xcall.ui.CallScreenActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import timber.log.Timber;

/**
 * Service to handle FCM notifications which is the main mechanism to send calls
 */
public class CallNotificationFCMService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    private static final String CALLERID_DATA_KEY = "callerId";
    private static final String NOTIF_TITLE_TEXT = "Someone is calling";
    private static final String NOTIF_BODY_TEXT = "Tap to pick up";
    private static Handler mNotifCancelHandler;


    @Override
    public void onCreate() {
        super.onCreate();
        if (mNotifCancelHandler == null) {
            mNotifCancelHandler = new Handler();
        }
    }

    /**
     * Triggered when a FCM message is received
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            String callerId = remoteMessage.getData().get(CALLERID_DATA_KEY);
            Log.d(TAG, "Receiving call from: " + callerId);

            createNotification(callerId);
        }
    }

    /**
     * creates notification
     * @param callerId contains the sinch id of the caller
     */
    private void createNotification(String callerId) {


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this)
                        .setAutoCancel(true)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(Notification.DEFAULT_SOUND)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(NOTIF_TITLE_TEXT)
                        .setContentText(NOTIF_BODY_TEXT);

        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(CallNotificationFCMService.this, CallScreenActivity.class);
        intent.putExtra(CALLERID_DATA_KEY, callerId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pending = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
        notificationBuilder.setContentIntent(pending);

        // using the same tag and Id causes the new notification to replace an existing one
        String tag = String.valueOf(callerId);
        String idString = Long.toString(System.currentTimeMillis());
        int id = Integer.valueOf(idString.substring(idString.length() - 5));
        Timber.d("Notification id" + id);

        mNotificationManager.notify(tag, id, notificationBuilder.build());

        removeNotificationAfter30s(tag, id);
    }

    /**
     * Removes the notification after 30 seconds as the call can no longer be picked up
     * @param tag tag of the notification
     * @param id id of the notification
     */
    private void removeNotificationAfter30s(final String tag, final int id) {

        final NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotifCancelHandler.postDelayed(new Runnable() {
            public void run() {
                Timber.d("Removing notification for " + tag + id);
                notificationManager.cancel(tag, id);
            }
        },30000);

    }
}
