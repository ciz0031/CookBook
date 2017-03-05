package com.example.erika.cookbook;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;

/**
 * Created by Erika on 7. 1. 2017.
 */

public class MyService extends Service {
    Bundle extras;
    CountDownTimer timer = null;
    int dobaPeceni;
    public static String str_receiver = "com.example.erika.cookbook.receiver";
    Intent receiver;
    public NotificationManager manager;
    public NotificationCompat.Builder builder;
    @Override
    public void onCreate() {
        super.onCreate();
        receiver = new Intent(str_receiver);
    }

    @Override
    public void onDestroy() {
        timer.cancel();
        super.onDestroy();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        extras = intent.getExtras();
        if (extras != null){
            //Log.d("Extras delivered", String.valueOf(extras.getInt("doba_peceni")));
            dobaPeceni = extras.getInt("doba_peceni");

            Intent notificationIntent = new Intent(this, CountdownTimer.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            final Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_stat_onesignal_default)
                    .setContentTitle("Kuchařka")
                    .setContentText("Běží odpočet času...")
                    .setContentIntent(pendingIntent).build();

            timer = new CountDownTimer(dobaPeceni, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    receiver.putExtra("countdown", millisUntilFinished);
                    sendBroadcast(receiver);

                }

                @Override
                public void onFinish() {
                    Log.d("countdowntimer finished", "ding dong");
                    builder = new NotificationCompat.Builder(MyService.this);

                    SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                    boolean vibrace = SP.getBoolean("vibrovat",true);
                    boolean nekonecnePrehravani = SP.getBoolean("nekonecnePrehravani", false);
                    String vyzvaneni = SP.getString("vyzvaneni", "content://settings/system/notification_sound");

                    if (vibrace == true) {
                        long[] pattern = {500, 500, 500, 500, 500, 500, 500, 500, 500};
                        builder.setVibrate(pattern);
                    }
                    builder.setStyle(new NotificationCompat.InboxStyle());
                    Uri alarmSound = Uri.parse(vyzvaneni);
                    builder.setSound(alarmSound);
                    Notification myNotification = builder.build();
                    if (nekonecnePrehravani == true) {
                        myNotification.flags |= Notification.FLAG_INSISTENT;
                        builder.setAutoCancel(true);
                    }else {
                        myNotification.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONLY_ALERT_ONCE;
                    }
                    builder.setSound(alarmSound);
                    manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    manager.notify(1, myNotification);
                    Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
                    //updateNotification();
                    Thread closeActivity = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                                stopSelf();
                            } catch (Exception e) {
                                e.getLocalizedMessage();
                            }
                        }
                    });
                    if (nekonecnePrehravani == false)
                        closeActivity.run();
                    timer.cancel();

                }
            };
            timer.start();

            startForeground(1337, notification);
        }

        return START_STICKY;
    }

    private Notification getMyActivityNotification(String text){
        // The PendingIntent to launch our activity if the user selects
        // this notification
        CharSequence title = "Kuchařka";
        Intent notificationIntent = new Intent(this, CountdownTimer.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        return new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_stat_onesignal_default)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent).getNotification();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
