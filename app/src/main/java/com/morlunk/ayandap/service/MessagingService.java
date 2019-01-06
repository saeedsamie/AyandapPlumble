package com.morlunk.ayandap.service;

import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.morlunk.ayandap.R;
import com.morlunk.ayandap.app.PlumbleActivity;

import java.util.Random;

import static com.morlunk.jumble.Constants.TAG;

public class MessagingService extends FirebaseMessagingService {
    private DatabaseReference mDatabase;

    public MessagingService() {
    }


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
//                scheduleJob();
            } else {
                // Handle message within 10 seconds
//                handleNow();
                Log.e("Message", "remote Message: " + remoteMessage.toString());
            }

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d("Message", "Message Notification Body: " + remoteMessage.getNotification().getBody());
            Intent intent = new Intent(this, PlumbleActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, null)
                    .setSmallIcon(R.drawable.mic_pic)
                    .setContentTitle(remoteMessage.getNotification().getTitle())
                    .setContentText(remoteMessage.getNotification().getBody()
                            + "\n icon " + remoteMessage.getNotification().getIcon())
                    .setPriority(remoteMessage.getPriority())
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(new Random().nextInt(), mBuilder.build());
        }
    }

    @Override
    public void onMessageSent(String s) {
        mDatabase = FirebaseDatabase.getInstance().getReference();
        super.onMessageSent(s);
    }

}
