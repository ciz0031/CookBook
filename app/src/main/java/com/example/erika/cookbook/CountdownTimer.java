package com.example.erika.cookbook;

import android.app.Activity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

public class CountdownTimer extends Activity {
    private Bundle extras;
    private TextView dobaPeceniTV;
    private TimePicker dobaPeceniTP;
    private ImageButton startButton;
    private ImageButton stopButton;
    private RelativeLayout RL;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown_timer);

        RL = (RelativeLayout) findViewById(R.id.activity_countdown_timer);

        /*RelativeLayout.LayoutParams layoutParams =
                (RelativeLayout.LayoutParams)dobaPeceniTV.getLayoutParams();
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);*/

        //podle toho odkud se aktivita vola se vytvori UI - kdyz z receptu, bude tam textView, kdyz z menu, bude tam timePicker
        extras = getIntent().getExtras();
        if(extras != null){
            //textView s moznosti pridani 1 minuty k casu
            String dobaPeceni = extras.getString("doba_peceni");
            if (dobaPeceni != null){
                dobaPeceniTV = new TextView(CountdownTimer.this);
                dobaPeceniTV.setText(dobaPeceni);

                //dobaPeceniTV.setLayoutParams(layoutParams);
                RL.addView(dobaPeceniTV);
            }
        }else {
            //timePicker
            dobaPeceniTP = new TimePicker(CountdownTimer.this);
            //dobaPeceniTP.setLayoutParams(layoutParams);
            RL.addView(dobaPeceniTP);
        }
    }
}
