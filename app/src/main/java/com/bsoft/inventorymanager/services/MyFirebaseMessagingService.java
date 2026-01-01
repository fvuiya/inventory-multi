package com.bsoft.inventorymanager.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.bsoft.inventorymanager.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getNotification() != null) {
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, "default")
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setAutoCancel(true);

            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel("default", "Default channel",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // If a user is logged in, update their token in Firestore
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();
            com.bsoft.inventorymanager.roles.RolesRepository repository = new com.bsoft.inventorymanager.roles.RolesRepository();
            repository.updateFCMToken(uid, token);
        }
    }
}
