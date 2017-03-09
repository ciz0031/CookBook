package com.example.erika.cookbook;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;

public class NewCountdownTimer extends Activity {
    private Bundle extras;
    private TimePicker dobaPeceniTP;
    private Button startButton;
    int dobaPeceni, dobaPeceniExtras;
    public NotificationManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer);

        startButton = (Button) findViewById(R.id.startButton);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        dobaPeceniTP = (TimePicker) findViewById(R.id.timePicker);
        dobaPeceniTP.setIs24HourView(true);
        dobaPeceniTP.setTag("timePickDobaPeceni");
        dobaPeceniTP.setVisibility(View.INVISIBLE);

        extras = getIntent().getExtras();
        if(extras != null){
            dobaPeceniExtras = extras.getInt("doba_peceni");
            dobaPeceni = dobaPeceniExtras;
            dobaPeceniTP.setVisibility(View.VISIBLE);
            int hour = dobaPeceni / 60;
            int minutes = dobaPeceni % 60;

            dobaPeceniTP.setCurrentHour(hour);
            dobaPeceniTP.setCurrentMinute(minutes);
        }else {
            dobaPeceniTP.setVisibility(View.VISIBLE);
            dobaPeceni = 0;
            dobaPeceniTP.setCurrentHour(0);
            dobaPeceniTP.setCurrentMinute(1);
        }


        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dobaPeceniTP.setVisibility(View.INVISIBLE);
                Bundle dataDobaPeceni = new Bundle();
                int hour = dobaPeceniTP.getCurrentHour();
                int minutes = dobaPeceniTP.getCurrentMinute();
                dobaPeceni = (hour * 60) + minutes;
                dataDobaPeceni.putInt("doba_peceni", dobaPeceni*60*1000);
                dataDobaPeceni.putString("nazev", dobaPeceni+" minut");
                dobaPeceniTP.setVisibility(View.INVISIBLE);
                startButton.setVisibility(View.INVISIBLE);
                startButton.setClickable(false);

                Intent i = new Intent(NewCountdownTimer.this, ListOfTimers.class);
                i.putExtras(dataDobaPeceni);
                startActivity(i);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
