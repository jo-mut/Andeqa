package com.andeqa.andeqa.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.andeqa.andeqa.R;
import com.andeqa.andeqa.creation.CreateCollectionPostActivity;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by J.EL on 7/15/2017.
 */

public class FirebaseMessaging extends FirebaseMessagingService {

    private static final String TAG = "FCM Service";
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO: Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated.
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        if (remoteMessage.getData().size() > 0){
            Log.d(TAG, "From: " + remoteMessage.getData());

        }

        if (remoteMessage.getNotification() != null){
            Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());
            sendNotification(remoteMessage.getNotification().getBody());
        }
    }

    private void sendNotification(String body) {
        // Add custom implementation, as needed.
        Intent intent = new Intent(this, CreateCollectionPostActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0/*request code*/, intent, PendingIntent.FLAG_ONE_SHOT);

        //set sound for notification
        Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_log)
                .setContentTitle("Andeqa")
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(notificationSound)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0 /*notification*/, builder.build());

    }

}
