package com.example.erika.cookbook;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Timer;

/**
 * Created by Erika on 7. 1. 2017.
 */

public class MyService extends Service {
    Bundle extras;
    CountDownTimer timer = null;
    int dobaPeceni;
    public static String str_receiver = "com.example.erika.cookbook.receiver";
    //Intent mIntent = new Intent(this, MyService.class);
    Intent receiver;
    public NotificationManager manager;
    public NotificationCompat.Builder builder;
    @Override
    public void onCreate() {
        super.onCreate();
        //extras = mIntent.getExtras();
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
            Log.d("Extras delivered", String.valueOf(extras.getInt("doba_peceni")));
            dobaPeceni = extras.getInt("doba_peceni");

            Intent notificationIntent = new Intent(this, CountdownTimer.class);

            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                    notificationIntent, 0);

            final Notification notification = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
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
                    long[] pattern = {500,500,500,500,500,500,500,500,500};
                    builder.setVibrate(pattern);
                    builder.setStyle(new NotificationCompat.InboxStyle());
                    Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                    builder.setSound(alarmSound);
                    manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.notify(1, builder.build());
                    Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
                    //updateNotification();
                    Thread closeActivity = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(5000);
                                stopSelf();
                            } catch (Exception e) {
                                e.getLocalizedMessage();
                            }
                        }
                    });
                    closeActivity.start();
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
        //TODO: vyřešit zobrazování ikonky běžícího časovače
        return new Notification.Builder(this)
                .setSmallIcon(R.mipmap.ic_stat_onesignal_default)
                .setContentTitle(title)
                .setContentText(text)
                .setContentIntent(pendingIntent).getNotification();
    }

    /**
     * This is the method that can be called to update the Notification
     */
    private void updateNotification() {
        String text = "Odpočet hotov!";

        Notification notification = getMyActivityNotification(text);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1337, notification);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
