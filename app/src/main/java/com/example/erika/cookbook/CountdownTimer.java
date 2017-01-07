package com.example.erika.cookbook;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
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
    MyCountDownTimer myCountDownTimer;
    int dobaPeceni;
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


        //podle toho odkud se aktivita vola se vytvori UI - kdyz z receptu, bude tam textView, kdyz z menu, bude tam timePicker
        extras = getIntent().getExtras();
        if(extras != null){
            //textView s moznosti pridani 1 minuty k casu
            dobaPeceni = extras.getInt("doba_peceni");
            dobaPeceniTV.setText(String.valueOf(dobaPeceni));
        }else {
            progressBar.setVisibility(View.INVISIBLE);
            dobaPeceni = 0;
            //timePicker
            dobaPeceniTP = new TimePicker(CountdownTimer.this);
            dobaPeceniTP.setIs24HourView(true);
            dobaPeceniTP.setCurrentHour(0);
            dobaPeceniTP.setCurrentMinute(0);
            dobaPeceniTP.setTag("timePickDobaPeceni");
            RL.addView(dobaPeceniTP);
        }

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                addOneMinuteButton.setVisibility(View.VISIBLE);
                //Log.d("dobaPeceni", dobaPeceni);
                if (dobaPeceni != 0){
                    myCountDownTimer = new MyCountDownTimer(dobaPeceni*60*1000, 1000);
                    //progressBar.setMax(dobaPeceni*60);
                }else{
                    int hour = dobaPeceniTP.getCurrentHour();
                    int minutes = dobaPeceniTP.getCurrentMinute();
                    dobaPeceni = (hour * 60) + minutes;
                    myCountDownTimer = new MyCountDownTimer(dobaPeceni*60*1000, 1000);
                    dobaPeceniTP.setVisibility(View.INVISIBLE);
                }
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);
                stopButton.setVisibility(View.VISIBLE);
                stopButton.setClickable(true);

                progressBar.setMax(dobaPeceni * 60);
                myCountDownTimer.start();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myCountDownTimer.cancel();
                stopButton.setVisibility(View.INVISIBLE);
                stopButton.setClickable(false);
                startButton.setVisibility(View.VISIBLE);
                startButton.setClickable(true);
                manager.cancelAll();
            }
        });

        addOneMinuteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pridani jedne minuty k casu odpoctu !
                manager.cancelAll();
                myCountDownTimer.cancel();
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
                myCountDownTimer = new MyCountDownTimer(dobaPeceniINT*1000, 1000);
                myCountDownTimer.start();
            }
        });

    }
    public class MyCountDownTimer extends CountDownTimer {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }


        @Override
        public void onTick(long millisUntilFinished) {

            int progress = (int) (millisUntilFinished/1000);//pocita od zhora dolu, po jednicku
            long millis = millisUntilFinished;

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
                manager.notify(1, builder.build());
                Toast.makeText(getApplicationContext(), R.string.done, Toast.LENGTH_LONG).show();
                stopButton.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.VISIBLE);
                stopButton.setClickable(false);
                startButton.setClickable(true);
                dobaPeceniTV.setText("00:00");
            }
        }
        //TODO: funkcni i po zavreni aplikace nebo switchnuti na jinou aktivitu !
        @Override
        public void onFinish() {
            //finish();
        }
    }
}
