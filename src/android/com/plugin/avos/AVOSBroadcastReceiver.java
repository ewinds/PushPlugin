package com.plugin.avos;

import org.json.JSONObject;

import com.avos.avoscloud.AVOSCloud;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

public class AVOSBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String packageName = AVOSCloud.applicationContext.getPackageName();

            if (intent.getAction().equals(packageName + ".UPDATE_STATUS")) {
                JSONObject json = new JSONObject(intent.getExtras().getString("com.avos.avoscloud.Data"));

                // if we are in the foreground, just surface the payload, else post it to the statusbar
                if (PushPlugin.isInForeground()) {
                    json.put("foreground", true);
                    // Send this JSON data to the JavaScript application above EVENT should be set to the msg type
                    // In this case this is the registration ID
                    PushPlugin.sendJavascript(json);
                }
                else {
                    json.put("foreground", false);
                    final String message = json.getString("alert");
                    Intent notificationIntent = new Intent(AVOSCloud.applicationContext, PushHandlerActivity.class);
                    notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    notificationIntent.putExtra("pushBundle", intent.getExtras());

                    PendingIntent pendingIntent =
                            PendingIntent.getActivity(AVOSCloud.applicationContext, 0, notificationIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT);
                    NotificationCompat.Builder mBuilder =
                            new NotificationCompat.Builder(AVOSCloud.applicationContext)
                                    .setSmallIcon(AVOSCloud.applicationContext.getApplicationInfo().icon)
                                    .setContentTitle(context.getString(AVOSCloud.applicationContext.getApplicationInfo().labelRes))
                                    .setContentText(message)
                                    .setTicker(message);
                    mBuilder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                    mBuilder.setContentIntent(pendingIntent);
                    mBuilder.setAutoCancel(true);

                    int mNotificationId = (new Random()).nextInt(500) + 1;
                    NotificationManager mNotifyMgr =
                            (NotificationManager) AVOSCloud.applicationContext
                                    .getSystemService(
                                            Context.NOTIFICATION_SERVICE);
                    mNotifyMgr.notify(mNotificationId, mBuilder.build());
                }
            }
        } catch (Exception e) {

        }
    }
}
