package com.example.erika.cookbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListOfTimers extends Activity {
    private ListView listOfTimersLV;
    private ArrayList<Timers> listOfTimers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_timers);
        listOfTimersLV = (ListView) findViewById(R.id.listOfTimersLV);
        Bundle extras = getIntent().getExtras();

        listOfTimers = new ArrayList<>();
        listOfTimers.add(new Timers("2 minuty", (2 * 60 * 1000), false));//2minuty
        listOfTimers.add(new Timers("10 minut", (10 * 60 * 1000), false));//10minut
        if (extras != null){
            String nazev = extras.getString("nazev");
            int doba = extras.getInt("doba_peceni");
            listOfTimers.add(new Timers(nazev, doba, false));
        }
        listOfTimersLV.setAdapter(new CountdownAdapter(ListOfTimers.this, listOfTimers));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_countdown_timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id== R.id.action_settings){
            Intent Settings = new Intent(this, Settings.class);
            startActivity(Settings);
        } else if (id == R.id.action_add){
            createNewTimer();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {

        super.onStart();
    }

    private void createNewTimer(){
        Intent timer = new Intent(this, CountdownTimer.class);
        startActivity(timer);
    }

    private class Timers {
        String name;
        long originalTime;
        long timeWhenDone;
        long timeToCountWhenPaused;
        long timeWithCurrent;
        boolean isRunning = false;
        boolean isPaused = false;
        boolean wasPaused = false;

        public Timers(String name, long originalTime, boolean isRunning) {
            this.name = name;
            this.originalTime = originalTime;
            this.isRunning = isRunning;
        }
    }

    public class CountdownAdapter extends ArrayAdapter<Timers> {
        private LayoutInflater lf;
        private List<ViewHolder> lstHolders;
        private Handler mHandler = new Handler();

        private Runnable updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                synchronized (lstHolders) {
                    long currentTime = System.currentTimeMillis();
                    for (ViewHolder holder : lstHolders) {
                        if (holder.mProduct.isRunning) {
                            if (holder.mProduct.wasPaused){
                                holder.updateTimeAfterPaused(currentTime);
                            }else {
                                holder.updateTimeRemaining(currentTime);
                            }
                        } else if (!holder.mProduct.isRunning) {
                            if (!holder.mProduct.isPaused) {
                                holder.setOriginalTime();
                                //mHandler.removeCallbacksAndMessages(updateRemainingTimeRunnable);
                            } else if (holder.mProduct.isPaused) {
                                holder.updateTimeRemaining(currentTime);
                                //mHandler.removeCallbacksAndMessages(updateRemainingTimeRunnable);
                            }
                        }
                    }
                }
            }
        };

        public CountdownAdapter(Context context, List<Timers> objects) {
            super(context, 0, objects);
            lf = LayoutInflater.from(context);
            lstHolders = new ArrayList<>();
        }

        private void startUpdateTimer() {
            Timer tmr = new Timer();
            tmr.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.post(updateRemainingTimeRunnable);
                }
            }, 100, 1000);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = lf.inflate(R.layout.timer_list_item, parent, false);
                holder.tvProduct = (TextView) convertView.findViewById(R.id.tvProduct);
                holder.tvTimeRemaining = (TextView) convertView.findViewById(R.id.tvTimeRemaining);
                holder.startB = (ImageButton) convertView.findViewById(R.id.startB);
                holder.stopB = (ImageButton) convertView.findViewById(R.id.stopB);
                holder.pauseB = (ImageButton) convertView.findViewById(R.id.pauseB);

                holder.startB.setImageResource(R.drawable.ic_action_play_arrow);
                holder.pauseB.setImageResource(R.drawable.ic_action_pause);
                holder.stopB.setImageResource(R.drawable.ic_action_stop);

                holder.tvTimeRemaining.setTag(position+"timer");
                holder.startB.setTag(position+"start");
                holder.stopB.setTag(position+"stop");
                holder.pauseB.setTag(position+"pause");

                holder.startB.setClickable(true);
                holder.stopB.setClickable(false);
                holder.pauseB.setClickable(false);

                synchronized (lstHolders) {
                    lstHolders.add(holder);
                }
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ViewHolder finalHolder = holder;
            holder.startB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalHolder.mProduct.isRunning = true;
                    if (finalHolder.mProduct.isPaused || finalHolder.mProduct.wasPaused){
                        finalHolder.mProduct.timeWithCurrent = System.currentTimeMillis() + finalHolder.mProduct.timeToCountWhenPaused;
                    }else {
                        finalHolder.mProduct.timeWithCurrent = System.currentTimeMillis() + finalHolder.mProduct.originalTime;
                    }

                    finalHolder.startB.setClickable(false);
                    finalHolder.pauseB.setClickable(true);
                    finalHolder.stopB.setClickable(true);
                    finalHolder.mProduct.timeWhenDone = System.currentTimeMillis() + finalHolder.mProduct.timeToCountWhenPaused;
                    finalHolder.mProduct.isPaused = false;
                    startUpdateTimer();
                    Log.d("startB", position+"start");
                }
            });
            holder.stopB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalHolder.mProduct.isRunning = false;
                    finalHolder.mProduct.isPaused = false;
                    finalHolder.stopB.setClickable(false);
                    finalHolder.pauseB.setClickable(false);
                    finalHolder.startB.setClickable(true);
                    mHandler.removeCallbacks(updateRemainingTimeRunnable);
                    finalHolder.mProduct.wasPaused = false;
                    startUpdateTimer();
                    Log.d("stopB", position+"stop");
                }
            });
            holder.pauseB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finalHolder.mProduct.isPaused = true;
                    finalHolder.mProduct.wasPaused = true;
                    finalHolder.mProduct.isRunning = false;
                    finalHolder.stopB.setClickable(true);
                    finalHolder.startB.setClickable(true);
                    finalHolder.pauseB.setClickable(false);
                    finalHolder.mProduct.timeToCountWhenPaused = finalHolder.mProduct.timeWithCurrent - System.currentTimeMillis();
                    mHandler.removeCallbacks(updateRemainingTimeRunnable);
                    startUpdateTimer();
                    Log.d("pauseB", position+"pause");
                }
            });
            holder.setData(getItem(position));
            return convertView;
        }
    }

    private class ViewHolder {
        TextView tvProduct;
        TextView tvTimeRemaining;
        ImageButton startB, stopB, pauseB;
        Timers mProduct;

        public void setData(Timers item) {
            mProduct = item;
            tvProduct.setText(item.name);
            setOriginalTime();
        }

        public void setOriginalTime() {
            long timeDiff = mProduct.originalTime;
            int seconds = (int) (timeDiff / 1000) % 60;
            int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
            int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
            String text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            tvTimeRemaining.setText(text);
        }

        public void updateTimeAfterPaused(long currentTime){

            long timeDiff = mProduct.timeWhenDone - currentTime;
            Log.d("timeWhenPaused", String.valueOf(timeDiff));
            setTime(timeDiff);
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff =  mProduct.timeWithCurrent - currentTime;
            if (mProduct.isPaused) {
                //mProduct.timeToCountWhenPaused = timeDiff;
                //Log.d("mProduct.timeToCountWhenPaused", String.valueOf(mProduct.timeToCountWhenPaused));
            }else {
                setTime(timeDiff);
            }
        }

        public void setTime(long timeDiff){
            if (timeDiff > 0) {
                int seconds = (int) (timeDiff / 1000) % 60;
                int minutes = (int) ((timeDiff / (1000 * 60)) % 60);
                int hours = (int) ((timeDiff / (1000 * 60 * 60)) % 24);
                String text = String.format("%02d:%02d:%02d", hours, minutes, seconds);
                tvTimeRemaining.setText(text);
            } else {
                if (mProduct.isRunning == false) {
                    setOriginalTime();
                } else {
                    tvTimeRemaining.setText("00:00:00");
                }
            }
        }
    }
}
