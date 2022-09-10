package com.msoft.mspartners;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class FirebaseMessagingService extends  com.google.firebase.messaging.FirebaseMessagingService {
    private static final String TAG = "FirebaseMsgService";
    private  String msg, title, link;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "onMessageReceived");

        Map map = remoteMessage.getData();
        title = (String) map.get("title");
        msg = (String) map.get("body");
        link = (String) map.get("link");

        /*
        title = remoteMessage.getNotification().getTitle();
        msg = remoteMessage.getNotification().getBody();
        link = remoteMessage.getNotification().getLink().toString();
        */

        if(link == null) {
            link = "";
        }
        sendNotification(title, msg, link);
    }

    public void sendNotification(String title, String message, String link) {
        SharedPreferences spfs = getApplicationContext().getSharedPreferences(getApplicationContext().getPackageName(), Context.MODE_PRIVATE);

        int noti_id = spfs.getInt("NOTI_ID", 0);
        SharedPreferences.Editor editor = spfs.edit();
        editor.putInt("NOTI_ID", noti_id + 1);
        editor.commit();

        boolean pushSound = true;
        Intent intent = new Intent(this, MainActivity.class);
        String channel_id = "mspartnersf_channel";
        //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("_LINK_", link);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, noti_id, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channel_id);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        if(pushSound) {
            builder.setSound(uri);
        } else {
            builder.setSound(null);
        }
        builder.setAutoCancel(true);
        builder.setVibrate(new long[] {1000, 1000, 1000, 1000, 1000});
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pendingIntent);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder = builder.setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher);
            //builder = builder.setContent(getCustomDesign(title, message));
        } else {
            builder = builder.setContentTitle(title).setContentText(message).setSmallIcon(R.mipmap.ic_launcher);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel;
            if(pushSound) {
                notificationChannel = new NotificationChannel(channel_id, "web_app", NotificationManager.IMPORTANCE_HIGH);
            }  else {
                notificationChannel = new NotificationChannel(channel_id, "web_app", NotificationManager.IMPORTANCE_LOW);
            }
            if(pushSound) {
                notificationChannel.setSound(uri, null);
            } else {
                notificationChannel.setSound(null, null);
            }
            notificationManager.createNotificationChannel(notificationChannel);
        }

        notificationManager.notify(noti_id, builder.build());
    }
}