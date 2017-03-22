package com.example.erika.cookbook;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.List;
import java.util.Random;

/**
 * Created by Erika on 10. 3. 2017.
 */

public class AlarmManagerBroadcastReceiver extends BroadcastReceiver {
    public NotificationCompat.Builder builder;
    public NotificationManager manager;
    @Override
    public void onReceive(final Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE, "TAG");
        wl.acquire();
        //upozorneni na vyprseni doby
        Log.d("countdowntimer finished", "ding dong");
        Intent listOfTimers = new Intent(context, ListOfTimers.class);
        listOfTimers.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(listOfTimers);
        builder = new NotificationCompat.Builder(context);

        SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(context);
        boolean vibrace = SP.getBoolean("vibrovat",true);
        boolean nekonecnePrehravani = SP.getBoolean("nekonecnePrehravani", false);
        String vyzvaneni = SP.getString("vyzvaneni", "content://settings/system/notification_sound");

        final Bundle extras = intent.getExtras();
        final int notificationID = extras.getInt("ID");
        manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(notificationID);


        if (vibrace == true) {
            long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
            builder.setVibrate(pattern);
        }
        builder.setStyle(new NotificationCompat.InboxStyle());
        Uri alarmSound = Uri.parse(vyzvaneni);
        builder.setSound(alarmSound);
        builder.setSmallIcon(R.mipmap.ic_stat_onesignal_default);
        builder.setContentTitle("Kuchařka");
        builder.setContentText("Odpočet času hotov!");
        final Notification myNotification = builder.build();

        if (nekonecnePrehravani == true) {
            myNotification.flags |= Notification.FLAG_INSISTENT;
            builder.setAutoCancel(true);
        }else {
            myNotification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
        }
        //builder.setSound(alarmSound);

        manager.notify(notificationID, myNotification);
        Toast.makeText(context, R.string.done, Toast.LENGTH_LONG).show();


        Thread closeActivity = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(4000);
                    if (extras != null) {
                        int _id = extras.getInt("ID");
                        cancelAlarm(context, _id);
                    }
                } catch (Exception e) {
                    e.getLocalizedMessage();
                }
            }
        });
        if (nekonecnePrehravani == false)
            closeActivity.start();

        wl.release();
    }

    public void setTimer(Context context, long timeWhenDone, int _id){
        AlarmManager am = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        intent.putExtra("ID", _id);
        PendingIntent pi = PendingIntent.getBroadcast(context, _id, intent, 0);
        am.setExact(AlarmManager.RTC_WAKEUP, timeWhenDone, pi);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_stat_onesignal_default)
                .setContentTitle("Kuchařka")
                .setContentText((_id+1)+": Běží odpočet času... " );
        Intent resultIntent = new Intent(context, ListOfTimers.class);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(ListOfTimers.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(_id, notification.build());
    }

    public void cancelAlarm(Context context, int _id){
        Intent intent = new Intent(context, AlarmManagerBroadcastReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, _id, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(sender);

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(_id);
        Log.d("CancelAlarm", "notification canceled"+_id);
    }


}
