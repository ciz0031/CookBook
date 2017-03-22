package com.example.erika.cookbook;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ListOfTimers extends Activity {
    private ListView listOfTimersLV;
    private ArrayList<Timers> listOfTimers;
    CountdownAdapter countdownAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_timers);
        listOfTimersLV = (ListView) findViewById(R.id.listOfTimersLV);
        Bundle extras = getIntent().getExtras();

        listOfTimers = new ArrayList<>();


        loadSavedPreferences();
        if (listOfTimers.size() < 1){
            listOfTimers.add(new Timers("2min", (2 * 60 * 1000), false, 0, 0));//2minuty
            listOfTimers.add(new Timers("10min", (10 * 60 * 1000), false, 0, 0));//10minut
            listOfTimers.add(new Timers("10sec", (10 * 1000), false, 0, 0));//10sec
        }
        if (extras != null){
            String nazev = extras.getString("nazev");
            int doba = extras.getInt("doba_peceni");
            listOfTimers.add(new Timers(nazev, doba, false, 0, 0));
        }
        countdownAdapter = new CountdownAdapter(ListOfTimers.this, listOfTimers);
        listOfTimersLV.setAdapter(countdownAdapter);

        //clearPref();
    }

    @Override
    protected void onResume() {
        long time = System.currentTimeMillis();
        for (int i = 0; i < listOfTimers.size(); i++){
            Timers timers = listOfTimers.get(i);
            Log.d("time when done"+i, timers.timeWhenDone+"");
            if (timers.isRunning && timers.timeWhenDone != 0 && timers.timeWhenDone - time > 0){
                //TODO timer is running so it needs to be updated in listView
                //

                //CountdownAdapter countdownAdapter = new CountdownAdapter(this, listOfTimers);
                countdownAdapter.startUpdateTimer();
            }
        }
        super.onResume();
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
    protected void onPause() {
        clearPref();
        long doba_peceni = 0;
        boolean isRunning = false;
        long timeWhenTimerDone = 0, timeWithCurrent = 0;
        Log.d("listOfTimers.size", String.valueOf(listOfTimers.size()));
        for (int i = 0; i < listOfTimers.size(); i++){
            Timers timers = listOfTimers.get(i);
            doba_peceni = timers.originalTime;
            isRunning = timers.isRunning;
            timeWhenTimerDone = timers.timeWhenDone;
            timeWithCurrent = timers.timeWithCurrent;
            savePreferences(String.valueOf(i), doba_peceni);
            saveIsRunning(String.valueOf(i), isRunning);
            saveTimeWhenDone(String.valueOf(i), timeWhenTimerDone);
            saveTimeWithCurrent(String.valueOf(i), timeWithCurrent);
        }
        //CountdownAdapter countdownAdapter = new CountdownAdapter(this, listOfTimers);
        countdownAdapter.mHandler.removeCallbacks(countdownAdapter.updateRemainingTimeRunnable);
        super.onPause();

    }

    @Override
    protected void onDestroy() {
        //CountdownAdapter countdownAdapter = new CountdownAdapter(this, listOfTimers);
        countdownAdapter.mHandler.removeCallbacks(countdownAdapter.updateRemainingTimeRunnable);
        super.onDestroy();
    }


    private void loadSavedPreferences() {
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimers", MODE_PRIVATE);
        SharedPreferences sharedPreferencesIsRunning = this.getSharedPreferences("listOfTimersRunning", MODE_PRIVATE);
        SharedPreferences sharedPreferencesTimeWhenDone = this.getSharedPreferences("listOfTimersTimeWhenDone", MODE_PRIVATE);
        SharedPreferences sharedPreferencesTimeWithCurrent = this.getSharedPreferences("listOfTimersTimeWithCurrent", MODE_PRIVATE);
        int sizeOfSharedPref = sharedPreferences.getAll().size();
        Log.d("sizeOfSharedPref", "velikost " + sizeOfSharedPref);
        long value, timeWhenDone, timeWithCurrent; boolean isRunning;

        for (int i = 0; i <= sizeOfSharedPref+1; i++){
            value = sharedPreferences.getLong(String.valueOf(i), 0);
            isRunning = sharedPreferencesIsRunning.getBoolean(String.valueOf(i), false);
            timeWhenDone = sharedPreferencesTimeWhenDone.getLong(String.valueOf(i), 0);
            timeWithCurrent = sharedPreferencesTimeWithCurrent.getLong(String.valueOf(i), 0);
            if (value != 0) {
                listOfTimers.add(new Timers(value/60/1000 + "min", value, isRunning, timeWhenDone, timeWithCurrent));
            }
        }
        listOfTimersLV.setAdapter(new CountdownAdapter(ListOfTimers.this, listOfTimers));

    }

    private void savePreferences(String key, long value) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimers", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, value);
        editor.commit();
    }

    private void saveIsRunning(String key, boolean isRunning) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimersRunning", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, isRunning);
        editor.commit();
    }

    private void saveTimeWhenDone(String key, long timeWhenDone) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimersTimeWhenDone", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, timeWhenDone);
        editor.commit();
    }

    private void saveTimeWithCurrent(String key, long timeWithCurrent) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimersTimeWithCurrent", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(key, timeWithCurrent);
        editor.commit();
    }

    public void clearPref(){
        SharedPreferences sharedPreferences = this.getSharedPreferences("listOfTimers", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.commit();

        SharedPreferences sharedPreferencesIsRunning = this.getSharedPreferences("listOfTimersRunning", MODE_PRIVATE);
        SharedPreferences.Editor editor2 = sharedPreferencesIsRunning.edit();
        editor2.clear();
        editor2.commit();

        SharedPreferences sharedPreferencesTimeWhenDone = this.getSharedPreferences("listOfTimersTimeWhenDone", MODE_PRIVATE);
        SharedPreferences.Editor editor3 = sharedPreferencesTimeWhenDone.edit();
        editor3.clear();
        editor3.commit();

        SharedPreferences sharedPreferencesTimeWithCurrent = this.getSharedPreferences("listOfTimersTimeWithCurrent", MODE_PRIVATE);
        SharedPreferences.Editor editor4 = sharedPreferencesTimeWithCurrent.edit();
        editor4.clear();
        editor4.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }



    private void createNewTimer(){
        Intent timer = new Intent(this, NewCountdownTimer.class);
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

        public Timers(String name, long originalTime, boolean isRunning, long timeWhenDone, long timeWithCurrent) {
            this.name = name;
            this.originalTime = originalTime;
            this.isRunning = isRunning;
            this.timeWhenDone = timeWhenDone;
            this.timeWithCurrent = timeWithCurrent;
        }
    }

    public class CountdownAdapter extends ArrayAdapter<Timers> {
        private LayoutInflater lf;
        private List<ViewHolder> lstHolders;
        public Handler mHandler = new Handler();
        private AlarmManagerBroadcastReceiver alarm;

        public Runnable updateRemainingTimeRunnable = new Runnable() {
            @Override
            public void run() {
                    synchronized (lstHolders) {
                        long currentTime = System.currentTimeMillis();
                        for (ViewHolder holder : lstHolders) {
                            if (holder.mProduct.isRunning) {
                                if (holder.mProduct.wasPaused) {
                                    if (holder.mProduct.timeWhenDone - currentTime < 0) {
                                        holder.mProduct.timeWhenDone = 0;
                                        mHandler.removeCallbacks(updateRemainingTimeRunnable);
                                    } else {
                                        holder.updateTimeAfterPaused(currentTime);
                                    }
                                } else {
                                    if (holder.mProduct.timeWithCurrent - currentTime < 0) {
                                        holder.mProduct.timeWhenDone = 0;
                                        mHandler.removeCallbacks(updateRemainingTimeRunnable);
                                    } else {
                                        holder.updateTimeRemaining(currentTime);
                                        //poresit kolikrat se toto vola !!
                                    }
                                }
                            } else if (!holder.mProduct.isRunning) {
                                if (!holder.mProduct.isPaused) {
                                    holder.mProduct.timeWhenDone = 0;
                                    holder.setOriginalTime();
                                    mHandler.removeCallbacksAndMessages(updateRemainingTimeRunnable);
                                } else if (holder.mProduct.isPaused) {
                                    //holder.updateTimeRemaining(currentTime);
                                    mHandler.removeCallbacksAndMessages(updateRemainingTimeRunnable);
                                }
                            }
                        }
                    }
            }
        };

        public CountdownAdapter(Context context, List<Timers> objects) {
            super(context, 0, objects);
            lf = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);//LayoutInflater.from(context);
            lstHolders = new ArrayList<>();
            alarm = new AlarmManagerBroadcastReceiver();
        }

        public void startUpdateTimer() {
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
                holder.deleteB = (ImageButton) convertView.findViewById(R.id.deleteB);

                holder.startB.setImageResource(R.drawable.ic_action_play_arrow);
                holder.pauseB.setImageResource(R.drawable.ic_action_pause);
                holder.stopB.setImageResource(R.drawable.ic_action_stop);
                holder.deleteB.setImageResource(R.drawable.ic_action_delete_forever);

                holder.tvTimeRemaining.setTag(position+"timer");
                holder.startB.setTag(position+"start");
                holder.stopB.setTag(position+"stop");
                holder.pauseB.setTag(position+"pause");
                holder.deleteB.setTag(position+"delete");

                holder.startB.setClickable(true);
                holder.stopB.setClickable(false);
                holder.pauseB.setClickable(false);
                holder.deleteB.setClickable(true);

                synchronized (lstHolders) {
                    lstHolders.add(holder);
                }
                convertView.setTag(holder);
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
                        finalHolder.mProduct.timeToCountWhenPaused = finalHolder.mProduct.originalTime;
                        finalHolder.mProduct.timeWithCurrent = System.currentTimeMillis() + finalHolder.mProduct.originalTime;
                    }

                    finalHolder.startB.setClickable(false);
                    finalHolder.pauseB.setClickable(true);
                    finalHolder.stopB.setClickable(true);
                    finalHolder.deleteB.setClickable(false);
                    finalHolder.mProduct.timeWhenDone = System.currentTimeMillis() + finalHolder.mProduct.timeToCountWhenPaused;
                    finalHolder.mProduct.isPaused = false;
                    alarm.setTimer(getApplication(), finalHolder.mProduct.timeWhenDone, position);
                    startUpdateTimer();
                    Log.d("startB", position+"start");
                    Log.d("time when done - on start", finalHolder.mProduct.timeWhenDone+"");
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
                    finalHolder.deleteB.setClickable(true);

                    finalHolder.mProduct.wasPaused = false;
                    finalHolder.mProduct.timeWhenDone = 0;
                    mHandler.removeCallbacks(updateRemainingTimeRunnable);
                    alarm.cancelAlarm(getApplication(), position);
                    //startUpdateTimer();
                    finalHolder.setOriginalTime();
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
                    mHandler.removeCallbacksAndMessages(updateRemainingTimeRunnable);
                    alarm.cancelAlarm(getApplication(), position);
                    Log.d("pauseB", position+"pause");
                }
            });
            holder.deleteB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    listOfTimers.remove(position);
                    notifyDataSetChanged();
                }
            });
            holder.setData(getItem(position));
            return convertView;
        }


        @Nullable
        @Override
        public Timers getItem(int position) {
            return listOfTimers.get(position);
        }

    }

    private class ViewHolder {
        TextView tvProduct;
        TextView tvTimeRemaining;
        ImageButton startB, stopB, pauseB, deleteB;
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
            //mProduct.timeWhenDone = 0;
        }

        public void updateTimeAfterPaused(long currentTime){
            long timeDiff = mProduct.timeWhenDone - currentTime;
            Log.d("timeWhenPaused", String.valueOf(timeDiff));
            setTime(timeDiff);
        }

        public void updateTimeRemaining(long currentTime) {
            long timeDiff =  mProduct.timeWithCurrent - currentTime;
            Log.d("timeRemainingToCount", String.valueOf(timeDiff));
            if (!mProduct.isPaused) {
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
