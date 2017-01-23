package com.example.erika.cookbook;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.util.TimeUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Time;
import java.util.concurrent.TimeUnit;

public class CountdownTimer extends Activity {
    private Bundle extras;
    private TextView dobaPeceniTV;
    private TimePicker dobaPeceniTP;
    private ImageButton startButton;
    private ImageButton stopButton;
    private RelativeLayout RL;
    private ProgressBar progressBar;
    private Button addOneMinuteButton;
    int dobaPeceni, dobaPeceniExtras;
    public NotificationManager manager;
    public NotificationCompat.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer);

        RL = (RelativeLayout) findViewById(R.id.activity_countdown_timer);
        startButton = (ImageButton) findViewById(R.id.startButton);
        stopButton = (ImageButton) findViewById(R.id.stopButton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        addOneMinuteButton = (Button) findViewById(R.id.addOneMinuteButton);
        stopButton.setVisibility(View.INVISIBLE);
        stopButton.setClickable(false);
        addOneMinuteButton.setVisibility(View.INVISIBLE);

        dobaPeceniTV = new TextView(CountdownTimer.this);
        dobaPeceniTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        dobaPeceniTV.setText("");
        dobaPeceniTV.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        dobaPeceniTV.setTextSize(35);
        RL.addView(dobaPeceniTV);

        builder = new NotificationCompat.Builder(this);
        long[] pattern = {500,500,500,500,500,500,500,500,500};
        builder.setVibrate(pattern);
        builder.setStyle(new NotificationCompat.InboxStyle());
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        dobaPeceniTP = new TimePicker(CountdownTimer.this);
        dobaPeceniTP.setIs24HourView(true);
        dobaPeceniTP.setTag("timePickDobaPeceni");
        RL.addView(dobaPeceniTP);

        dobaPeceniTP.setVisibility(View.INVISIBLE);

        //podle toho odkud se aktivita vola se vytvori UI - kdyz z receptu, bude tam textView, kdyz z menu, bude tam timePicker

        if (isMyServiceRunning(MyService.class) == true){
            progressBar.setVisibility(View.VISIBLE);
            addOneMinuteButton.setVisibility(View.VISIBLE);
            startButton.setVisibility(View.INVISIBLE);
            startButton.setClickable(false);
            stopButton.setVisibility(View.VISIBLE);
            stopButton.setClickable(true);
            Log.d("service", "service running!");
        }else if (isMyServiceRunning(MyService.class) == false){
            Log.d("service", "service not running!");
            extras = getIntent().getExtras();
            if(extras != null){
                //textView s moznosti pridani 1 minuty k casu
                dobaPeceniExtras = extras.getInt("doba_peceni");
                dobaPeceni = dobaPeceniExtras;
                dobaPeceniTV.setText(String.valueOf(dobaPeceni));
            }else {
                progressBar.setVisibility(View.INVISIBLE);
                dobaPeceniTP.setVisibility(View.VISIBLE);
                dobaPeceni = 0;
                dobaPeceniTP.setCurrentHour(0);
                dobaPeceniTP.setCurrentMinute(0);

            }
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                addOneMinuteButton.setVisibility(View.VISIBLE);
                addOneMinuteButton.setClickable(true);
                dobaPeceniTP.setVisibility(View.INVISIBLE);
                dobaPeceniTV.setVisibility(View.VISIBLE);
                //Log.d("dobaPeceni", dobaPeceni);
                Bundle dataDobaPeceni = new Bundle();
                if (dobaPeceniExtras != 0){
                    dataDobaPeceni.putInt("doba_peceni", dobaPeceni*60*1000);
                }else{
                    int hour = dobaPeceniTP.getCurrentHour();
                    int minutes = dobaPeceniTP.getCurrentMinute();
                    dobaPeceni = (hour * 60) + minutes;
                    dataDobaPeceni.putInt("doba_peceni", dobaPeceni*60*1000);
                    dobaPeceniTP.setVisibility(View.INVISIBLE);
                }
                Log.d("dobaPeceni", String.valueOf(dobaPeceni));
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
                stopButton.setVisibility(View.VISIBLE);
                stopButton.setClickable(true);
                progressBar.setMax(dobaPeceni * 60);

                Intent i = new Intent(CountdownTimer.this, MyService.class);
                i.putExtras(dataDobaPeceni);
                startService(i);
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService(new Intent(CountdownTimer.this, MyService.class));
                stopButton.setVisibility(View.INVISIBLE);
                stopButton.setClickable(false);
                startButton.setVisibility(View.VISIBLE);
                startButton.setClickable(true);
                manager.cancelAll();
                dobaPeceniTV.setVisibility(View.INVISIBLE);
                if (dobaPeceniExtras == 0){
                    progressBar.setVisibility(View.INVISIBLE);
                    dobaPeceniTP.setVisibility(View.VISIBLE);
                    dobaPeceniTP.setCurrentHour(0);
                    dobaPeceniTP.setCurrentMinute(0);

                    addOneMinuteButton.setVisibility(View.INVISIBLE);
                    addOneMinuteButton.setClickable(false);
                }else if (dobaPeceniExtras != 0){
                    progressBar.setVisibility(View.INVISIBLE);
                    dobaPeceniTP.setVisibility(View.VISIBLE);
                    dobaPeceniTP.setCurrentHour(0);
                    dobaPeceniTP.setCurrentMinute(dobaPeceni);
                    addOneMinuteButton.setVisibility(View.INVISIBLE);
                    addOneMinuteButton.setClickable(false);
                }
            }
        });

        addOneMinuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pridani jedne minuty k casu odpoctu !
                manager.cancelAll();
                stopService(new Intent(CountdownTimer.this, MyService.class));
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
                stopButton.setVisibility(View.VISIBLE);
                stopButton.setClickable(true);
                String dobaPeceni = dobaPeceniTV.getText().toString();
                String[] separated = dobaPeceni.split(":");
                String minute = separated[0];
                int seconds = Integer.parseInt(separated[1]);
                int minutes = Integer.parseInt(minute) * 60;//prevedeno na sekundy
                int dobaPeceniINT = minutes + seconds + 60;//v sekundach
                progressBar.setMax(dobaPeceniINT);
                Bundle dataDobaPeceni = new Bundle();
                dataDobaPeceni.putInt("doba_peceni", dobaPeceniINT*1000);
                Intent i = new Intent(CountdownTimer.this, MyService.class);
                i.putExtras(dataDobaPeceni);
                startService(i);
            }
        });

    }
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateGUI(intent); // or whatever method used to update your GUI fields
        }
    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //registerReceiver(broadcastReceiver, new IntentFilter(MyService.str_receiver));
        Log.d("back button", "back button pressed");
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(MyService.str_receiver));
        Log.d("receiver", "Registered broacast receiver-onresume");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.d("receiver", "Unregistered broacast receiver-onpause");
    }

    @Override
    public void onStop() {
        try {
            unregisterReceiver(broadcastReceiver);
            Log.d("receiver", "Unregistered broacast receiver-onstop");
        } catch (Exception e) {
            // Receiver was probably already stopped in onPause()
        }
        super.onStop();
    }
    /*@Override
    public void onDestroy() {
        stopService(new Intent(this, MyService.class));
        serviceRunning =false;
        Log.d("service", "stop service called");
        //Log.i(TAG, "Stopped service");
        super.onDestroy();
    }*/

    public void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millis = intent.getLongExtra("countdown", 0);
            int done = intent.getIntExtra("done", 0);
            int progress = (int) (millis/1000);
            progressBar.setProgress(progressBar.getMax()-progress);

            if (dobaPeceni == 0){//tzn byl zobrazen timePicker, takze neni vytvoren TextView
                dobaPeceniTV.setText(String.valueOf(dobaPeceni));

            }
            String minuteSecond = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                    TimeUnit.MILLISECONDS.toSeconds(millis)- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            dobaPeceniTV.setText(minuteSecond);

            Log.d("progress", String.valueOf(progress));
            if (progress <= 1){
                //ring !! vibrate !! :)
                //manager.notify(1, builder.build());

                stopButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                stopButton.setClickable(false);
                startButton.setClickable(true);
                dobaPeceniTV.setText("00:00");
//                stopService(new Intent(this, MyService.class));
            }

            if (done != 0) {
                dobaPeceniTV.setText("00:00");
            }
            //Log.i(TAG, "Countdown seconds remaining: " +  millisUntilFinished / 1000);
        }
    }

}
